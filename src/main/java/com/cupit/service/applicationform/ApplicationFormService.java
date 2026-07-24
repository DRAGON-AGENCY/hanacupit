package com.cupit.service.applicationform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;
import com.cupit.csv.parser.ApplicationFormCsvParser;
import com.cupit.csv.validator.ApplicationFormCsvValidator;
import com.cupit.model.ApplicationFormInput;
import com.cupit.model.MemberInfo;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.repository.MemberInfoRepository;
import com.cupit.repository.PaygateMappingRepository;

/**
 * 「各決済会社所定申込フォーム作成」画面のビジネスロジックを担うサービス。
 * アップロードされたINPUT CSVを検証・解析し、m_member_info・m_paygate_store_mappingと
 * 取引コードで突き合わせたうえで、指定された決済会社所定フォーマットのExcelを生成する。
 * INPUT CSVの内容はDBへ永続化しない（アップロードごとにその場でExcelへ変換する
 * 一時的な処理のため）。
 */
@Service
public class ApplicationFormService {

    /**
     * 画面の「申請先」セレクトに対応する出力先。
     */
    public enum Destination {
        JCB,
        SMCC_KAMEI,
        SMCC_TENPO,
    }

    private final ApplicationFormCsvValidator csvValidator;
    private final ApplicationFormCsvParser csvParser;
    private final MemberInfoRepository memberInfoRepository;
    private final PaygateMappingRepository paygateMappingRepository;
    private final ApplicationFormDeriveLogic deriveLogic;
    private final ApplicationFormJcbWriter jcbWriter;
    private final ApplicationFormSmccKameiWriter smccKameiWriter;
    private final ApplicationFormSmccTenpoWriter smccTenpoWriter;

    public ApplicationFormService(
            ApplicationFormCsvValidator csvValidator,
            ApplicationFormCsvParser csvParser,
            MemberInfoRepository memberInfoRepository,
            PaygateMappingRepository paygateMappingRepository,
            ApplicationFormDeriveLogic deriveLogic,
            ApplicationFormJcbWriter jcbWriter,
            ApplicationFormSmccKameiWriter smccKameiWriter,
            ApplicationFormSmccTenpoWriter smccTenpoWriter) {
        this.csvValidator = csvValidator;
        this.csvParser = csvParser;
        this.memberInfoRepository = memberInfoRepository;
        this.paygateMappingRepository = paygateMappingRepository;
        this.deriveLogic = deriveLogic;
        this.jcbWriter = jcbWriter;
        this.smccKameiWriter = smccKameiWriter;
        this.smccTenpoWriter = smccTenpoWriter;
    }

    /**
     * INPUT CSVを検証・解析し、指定された決済会社所定フォーマットのExcelを生成する。
     *
     * @param destination 申請先（JCB／SMCC_KAMEI／SMCC_TENPO）
     * @param file        アップロードファイル
     * @return 生成結果（成功時はExcelバイト列、失敗時はエラー内容）
     * @throws IOException ファイル読み込みエラー
     */
    public ApplicationFormGenerateResult generate(
            Destination destination, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ApplicationFormGenerateResult.error("ファイルが選択されていません。", List.of());
        }

        CsvValidationResult validationResult = csvValidator.validate(file);
        if (validationResult.isFatal()) {
            return ApplicationFormGenerateResult.error(
                    "フォーマット検証エラー: " + buildFatalDetailMessage(validationResult),
                    validationResult.getErrors());
        }

        ApplicationFormCsvParser.ParseResult parseResult = csvParser.parse(file);
        if (parseResult.getRecords().isEmpty()) {
            return ApplicationFormGenerateResult.error(
                    buildNoRegistrableRowsMessage(parseResult.getErrors()),
                    parseResult.getErrors());
        }

        List<ApplicationFormRowContext> rows = buildRowContexts(parseResult.getRecords());
        byte[] excelBytes = writeExcel(destination, rows);

        return ApplicationFormGenerateResult.success(
                excelBytes, rows.size(), parseResult.getTotalRowCount(),
                parseResult.getErrors());
    }

    private byte[] writeExcel(Destination destination, List<ApplicationFormRowContext> rows) {
        return switch (destination) {
            case JCB -> jcbWriter.write(rows);
            case SMCC_KAMEI -> smccKameiWriter.write(rows);
            case SMCC_TENPO -> smccTenpoWriter.write(rows);
        };
    }

    private List<ApplicationFormRowContext> buildRowContexts(List<ApplicationFormInput> records) {
        List<ApplicationFormRowContext> rows = new ArrayList<>();
        int rowSequence = 1;
        for (ApplicationFormInput input : records) {
            MemberInfo memberInfo = memberInfoRepository.findById(input.getTradeCode()).orElse(null);
            List<PaygateStoreMapping> paygateList =
                    paygateMappingRepository.findByTradeCodeOrderByTerminalId(input.getTradeCode());
            PaygateStoreMapping paygate = resolvePaygate(paygateList);
            boolean existingContract = !paygateList.isEmpty();
            Map<String, String> derivedValues = deriveLogic.compute(input, existingContract);
            rows.add(new ApplicationFormRowContext(
                    input, memberInfo, paygate, derivedValues, rowSequence));
            rowSequence++;
        }
        return rows;
    }

    private PaygateStoreMapping resolvePaygate(List<PaygateStoreMapping> paygateList) {
        if (paygateList.isEmpty()) {
            return null;
        }
        return paygateList.stream()
                .filter(p -> p.getJcbMerchantNo() != null && !p.getJcbMerchantNo().isBlank())
                .findFirst()
                .orElse(paygateList.get(0));
    }

    private String buildFatalDetailMessage(CsvValidationResult validationResult) {
        if (validationResult.getErrors().isEmpty()) {
            return "フォーマットエラー";
        }
        CsvValidationError error = validationResult.getErrors().get(0);
        return error.getRowNumber() > 0
                ? error.getRowNumber() + "行目: " + error.getMessage()
                : error.getMessage();
    }

    /**
     * 全行がエラーでスキップされた場合、件数だけの通知では原因が分からないため、
     * 実際のエラー内容をそのまま返す（同じ内容のメッセージは1つにまとめる）。
     */
    private String buildNoRegistrableRowsMessage(List<CsvValidationError> errors) {
        if (errors.isEmpty()) {
            return "登録可能な行がありません。";
        }
        return errors.stream()
                .map(CsvValidationError::getMessage)
                .distinct()
                .limit(5)
                .collect(Collectors.joining(" "));
    }

}
