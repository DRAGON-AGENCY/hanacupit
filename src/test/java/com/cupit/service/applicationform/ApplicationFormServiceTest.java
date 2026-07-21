package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;
import com.cupit.csv.parser.ApplicationFormCsvParser;
import com.cupit.csv.validator.ApplicationFormCsvValidator;
import com.cupit.model.ApplicationFormInput;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.repository.MemberInfoRepository;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.service.applicationform.ApplicationFormService.Destination;

/**
 * {@link ApplicationFormService} のテスト。ファイル未選択・致命的フォーマットエラー・
 * 登録可能な行が0件の早期リターン、申請先ごとのWriter呼び分け、行ごとの
 * m_member_info／m_paygate_store_mapping突き合わせ（既存契約有無の判定・
 * jcb_merchant_no優先のPAYGATE解決）を、各依存をモック化して検証する。
 */
@ExtendWith(MockitoExtension.class)
class ApplicationFormServiceTest {

    @Mock
    private ApplicationFormCsvValidator csvValidator;

    @Mock
    private ApplicationFormCsvParser csvParser;

    @Mock
    private MemberInfoRepository memberInfoRepository;

    @Mock
    private PaygateMappingRepository paygateMappingRepository;

    @Mock
    private ApplicationFormDeriveLogic deriveLogic;

    @Mock
    private ApplicationFormJcbWriter jcbWriter;

    @Mock
    private ApplicationFormSmccKameiWriter smccKameiWriter;

    @Mock
    private ApplicationFormSmccTenpoWriter smccTenpoWriter;

    private ApplicationFormService service;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        service = new ApplicationFormService(
                csvValidator, csvParser, memberInfoRepository, paygateMappingRepository,
                deriveLogic, jcbWriter, smccKameiWriter, smccTenpoWriter);
        file = new MockMultipartFile("file", "application_form_input.csv", "text/csv", new byte[] {1});
    }

    @Test
    void returnsErrorWhenFileIsNull() throws Exception {
        ApplicationFormGenerateResult result = service.generate(Destination.JCB, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("ファイルが選択されていません");
    }

    @Test
    void returnsErrorWhenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile =
                new MockMultipartFile("file", "application_form_input.csv", "text/csv", new byte[0]);

        ApplicationFormGenerateResult result = service.generate(Destination.JCB, emptyFile);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("ファイルが選択されていません");
    }

    @Test
    void returnsFatalValidationErrorMessageWithoutCallingParser() throws Exception {
        CsvValidationResult fatalResult = new CsvValidationResult();
        fatalResult.addError(new CsvValidationError(1, "", "ファイルの拡張子が不正です。"));
        fatalResult.markFatal();
        when(csvValidator.validate(file)).thenReturn(fatalResult);

        ApplicationFormGenerateResult result = service.generate(Destination.JCB, file);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("フォーマット検証エラー");
        assertThat(result.getErrorMessage()).contains("ファイルの拡張子が不正です");
        verify(csvParser, never()).parse(any());
    }

    @Test
    void returnsErrorWhenNoRegistrableRowsAfterParsing() throws Exception {
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(2, "取引コード", "取引コードは必須です。"));
        when(csvParser.parse(file)).thenReturn(
                new ApplicationFormCsvParser.ParseResult(List.of(), errors, 1));

        ApplicationFormGenerateResult result = service.generate(Destination.JCB, file);

        assertThat(result.isSuccess()).isFalse();
        // 件数だけの通知では原因が分からないため、実際のエラー内容をそのまま返す
        assertThat(result.getErrorMessage()).isEqualTo("取引コードは必須です。");
    }

    @Test
    void joinsDistinctErrorMessagesWhenNoRegistrableRowsHaveDifferentCauses() throws Exception {
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(2, "取引コード", "取引コードは必須です。"),
                new CsvValidationError(3, "サービス開始希望日", "日付変換エラーです。"));
        when(csvParser.parse(file)).thenReturn(
                new ApplicationFormCsvParser.ParseResult(List.of(), errors, 2));

        ApplicationFormGenerateResult result = service.generate(Destination.JCB, file);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("取引コードは必須です。 日付変換エラーです。");
    }

    @Test
    void callsJcbWriterForJcbDestination() throws Exception {
        setUpSingleRecordNoMemberInfoNoPaygate("35-232");
        when(jcbWriter.write(any())).thenReturn(new byte[] {9});

        ApplicationFormGenerateResult result = service.generate(Destination.JCB, file);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getExcelBytes()).isEqualTo(new byte[] {9});
        verify(jcbWriter).write(any());
        verify(smccKameiWriter, never()).write(any());
        verify(smccTenpoWriter, never()).write(any());
    }

    @Test
    void callsSmccKameiWriterForSmccKameiDestination() throws Exception {
        setUpSingleRecordNoMemberInfoNoPaygate("35-232");
        when(smccKameiWriter.write(any())).thenReturn(new byte[] {9});

        ApplicationFormGenerateResult result = service.generate(Destination.SMCC_KAMEI, file);

        assertThat(result.isSuccess()).isTrue();
        verify(smccKameiWriter).write(any());
        verify(jcbWriter, never()).write(any());
        verify(smccTenpoWriter, never()).write(any());
    }

    @Test
    void callsSmccTenpoWriterForSmccTenpoDestination() throws Exception {
        setUpSingleRecordNoMemberInfoNoPaygate("35-232");
        when(smccTenpoWriter.write(any())).thenReturn(new byte[] {9});

        ApplicationFormGenerateResult result = service.generate(Destination.SMCC_TENPO, file);

        assertThat(result.isSuccess()).isTrue();
        verify(smccTenpoWriter).write(any());
        verify(jcbWriter, never()).write(any());
        verify(smccKameiWriter, never()).write(any());
    }

    @Test
    void looksUpMemberInfoAndPaygateByTradeCode() throws Exception {
        setUpSingleRecordNoMemberInfoNoPaygate("35-232");
        when(jcbWriter.write(any())).thenReturn(new byte[0]);

        service.generate(Destination.JCB, file);

        verify(memberInfoRepository).findById("35-232");
        verify(paygateMappingRepository).findByTradeCodeOrderByTerminalId("35-232");
    }

    @Test
    void setsExistingContractTrueWhenPaygateListNonEmpty() throws Exception {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setTradeCode("35-232");
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(csvParser.parse(file)).thenReturn(
                new ApplicationFormCsvParser.ParseResult(List.of(input), List.of(), 1));
        when(memberInfoRepository.findById("35-232")).thenReturn(Optional.empty());
        PaygateStoreMapping paygate = new PaygateStoreMapping();
        paygate.setJcbMerchantNo("1234567890");
        when(paygateMappingRepository.findByTradeCodeOrderByTerminalId("35-232"))
                .thenReturn(List.of(paygate));
        when(deriveLogic.compute(any(), anyBoolean())).thenReturn(Map.of());
        when(jcbWriter.write(any())).thenReturn(new byte[0]);

        service.generate(Destination.JCB, file);

        verify(deriveLogic).compute(eq(input), eq(true));
    }

    @Test
    void resolvesPaygatePreferringNonBlankJcbMerchantNo() throws Exception {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setTradeCode("35-232");
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(csvParser.parse(file)).thenReturn(
                new ApplicationFormCsvParser.ParseResult(List.of(input), List.of(), 1));
        when(memberInfoRepository.findById("35-232")).thenReturn(Optional.empty());
        PaygateStoreMapping blank = new PaygateStoreMapping();
        blank.setJcbMerchantNo("");
        blank.setTerminalId("T1");
        PaygateStoreMapping nonBlank = new PaygateStoreMapping();
        nonBlank.setJcbMerchantNo("1234567890");
        nonBlank.setTerminalId("T2");
        when(paygateMappingRepository.findByTradeCodeOrderByTerminalId("35-232"))
                .thenReturn(List.of(blank, nonBlank));
        when(deriveLogic.compute(any(), anyBoolean())).thenReturn(Map.of());
        when(jcbWriter.write(any())).thenReturn(new byte[0]);

        service.generate(Destination.JCB, file);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ApplicationFormRowContext>> captor = ArgumentCaptor.forClass(List.class);
        verify(jcbWriter).write(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getPaygateStoreMapping().getJcbMerchantNo())
                .isEqualTo("1234567890");
    }

    @Test
    void returnsSuccessResultWithRowCounts() throws Exception {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setTradeCode("35-232");
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        List<CsvValidationError> parseErrors = List.of(
                new CsvValidationError(3, "取引コード", "取引コードは必須です。"));
        when(csvParser.parse(file)).thenReturn(
                new ApplicationFormCsvParser.ParseResult(List.of(input), parseErrors, 2));
        when(memberInfoRepository.findById("35-232")).thenReturn(Optional.empty());
        when(paygateMappingRepository.findByTradeCodeOrderByTerminalId("35-232"))
                .thenReturn(List.of());
        when(deriveLogic.compute(any(), anyBoolean())).thenReturn(Map.of());
        when(jcbWriter.write(any())).thenReturn(new byte[] {1, 2, 3});

        ApplicationFormGenerateResult result = service.generate(Destination.JCB, file);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getTotalRowCount()).isEqualTo(2);
        assertThat(result.getErrors()).hasSize(1);
    }

    private void setUpSingleRecordNoMemberInfoNoPaygate(String tradeCode) throws Exception {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setTradeCode(tradeCode);
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(csvParser.parse(file)).thenReturn(
                new ApplicationFormCsvParser.ParseResult(List.of(input), List.of(), 1));
        when(memberInfoRepository.findById(tradeCode)).thenReturn(Optional.empty());
        when(paygateMappingRepository.findByTradeCodeOrderByTerminalId(tradeCode))
                .thenReturn(List.of());
        when(deriveLogic.compute(any(), anyBoolean())).thenReturn(Map.of());
    }

}
