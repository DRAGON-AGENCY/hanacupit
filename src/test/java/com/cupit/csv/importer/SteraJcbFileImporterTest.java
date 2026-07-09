package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.model.ImportBatch;
import com.cupit.model.SteraJcbSalesDetail;
import com.cupit.model.SteraTerminal;
import com.cupit.repository.SteraJcbSalesDetailRepository;
import com.cupit.repository.SteraTerminalRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link SteraJcbFileImporter} のテスト。加盟店番号の全角→半角正規化、
 * m_stera_terminal からの有効端末解決（0件・複数件はスキップ）、部分登録、
 * extractLookupKey／deleteBatchData を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraJcbFileImporterTest {

    /** stera_jcb_valid.csv の加盟店番号 ２１８１－５００－５１－０００３４ の正規化後の値。 */
    private static final String MERCHANT_A_NORMALIZED = "21815005100034";
    /** stera_jcb_partial.csv の2行目 ２１８１－５００－５１－０００４２ の正規化後の値。 */
    private static final String MERCHANT_B_NORMALIZED = "21815005100042";

    @Mock
    private SteraJcbSalesDetailRepository steraJcbSalesDetailRepository;

    @Mock
    private SteraTerminalRepository steraTerminalRepository;

    private SteraJcbFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SteraJcbFileImporter(steraJcbSalesDetailRepository, steraTerminalRepository);
    }

    @Test
    void importsValidFileAndResolvesTradeCode() throws Exception {
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_A_NORMALIZED))
                .thenReturn(List.of(activeTerminal("01-001")));

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_jcb_valid.csv"), batch(10, "user001"));

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.hasErrors()).isFalse();

        ArgumentCaptor<SteraJcbSalesDetail> captor = ArgumentCaptor.forClass(SteraJcbSalesDetail.class);
        verify(steraJcbSalesDetailRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        SteraJcbSalesDetail first = captor.getAllValues().get(0);
        assertThat(first.getTradeCode()).isEqualTo("01-001");
        assertThat(first.getBatchId()).isEqualTo(10);
        assertThat(first.getStoreNumber()).isEqualTo("２１８１－５００－５１－０００３４");
        assertThat(first.getSalesAmount()).isEqualTo(25000);
        assertThat(first.getUpdateEmployee()).isEqualTo("user001");
    }

    @Test
    void normalizesFullWidthMerchantNoBeforeLookup() throws Exception {
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_A_NORMALIZED))
                .thenReturn(List.of(activeTerminal("01-001")));

        importer.importFile(CsvFiles.fromClasspath("stera_jcb_valid.csv"), batch(10, "user001"));

        verify(steraTerminalRepository, org.mockito.Mockito.atLeastOnce())
                .findByJcbMerchantNo(MERCHANT_A_NORMALIZED);
    }

    @Test
    void skipsRowWhenNoActiveTerminalExists() throws Exception {
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_A_NORMALIZED))
                .thenReturn(List.of());

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_jcb_valid.csv"), batch(10, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getMessage()).contains("有効な端末情報");
    }

    @Test
    void skipsRowWhenMultipleActiveTerminalsExist() throws Exception {
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_A_NORMALIZED))
                .thenReturn(List.of(activeTerminal("01-001"), activeTerminal("09-999")));

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_jcb_valid.csv"), batch(10, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("複数件存在");
    }

    @Test
    void filtersOutTerminalsWithEndDateAndKeepsActiveOne() throws Exception {
        SteraTerminal expired = activeTerminal("09-999");
        expired.setTerminalEndDate(LocalDate.of(2025, 3, 31));
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_A_NORMALIZED))
                .thenReturn(List.of(expired, activeTerminal("01-001")));

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_jcb_valid.csv"), batch(10, "user001"));

        assertThat(result.getSuccessCount()).isEqualTo(2);
        ArgumentCaptor<SteraJcbSalesDetail> captor = ArgumentCaptor.forClass(SteraJcbSalesDetail.class);
        verify(steraJcbSalesDetailRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getTradeCode()).isEqualTo("01-001");
    }

    @Test
    void performsPartialRegistrationSkippingErrorRows() throws Exception {
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_A_NORMALIZED))
                .thenReturn(List.of(activeTerminal("01-001")));
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_B_NORMALIZED))
                .thenReturn(List.of());

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_jcb_partial.csv"), batch(10, "user001"));

        // 1行目=正常登録、2行目=マッピング無し、3行目=売上金額が非数値
        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getTotalRowCount()).isEqualTo(3);
    }

    @Test
    void reportsColumnCountErrorForShortRow() throws Exception {
        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv",
                        "加盟店名称,加盟店番号,ご契約カード会社",
                        "いなば生花店,２１８１－５００－５１－０００３４"),
                batch(10, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正");
    }

    @Test
    void skipsBlankLineWithoutError() throws Exception {
        when(steraTerminalRepository.findByJcbMerchantNo(MERCHANT_A_NORMALIZED))
                .thenReturn(List.of(activeTerminal("01-001")));

        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv",
                        "加盟店名称,加盟店番号,ご契約カード会社,お支払方法,お取扱カード名,支払区分,売上方法,集計日,売上件数,売上金額（円）",
                        "",
                        "いなば生花店,２１８１－５００－５１－０００３４,■ＪＣＢ,◆クレジット,【ＪＣＢカード】,１回払い,Ｃ,11/08,1,25000"),
                batch(10, "user001"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void extractLookupKeyReturnsMerchantNoOfSecondRow() throws Exception {
        String key = importer.extractLookupKey(CsvFiles.fromClasspath("stera_jcb_valid.csv"));

        assertThat(key).isEqualTo("２１８１－５００－５１－０００３４");
    }

    @Test
    void extractLookupKeyThrowsWhenNoDataRow() throws Exception {
        assertThatThrownBy(() -> importer.extractLookupKey(
                CsvFiles.utf8Bom("x.csv", "加盟店名称,加盟店番号")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("データ行がありません");
    }

    @Test
    void extractLookupKeyThrowsWhenMerchantNoIsBlank() throws Exception {
        assertThatThrownBy(() -> importer.extractLookupKey(
                CsvFiles.utf8Bom("x.csv", "加盟店名称,加盟店番号", "いなば生花店,")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("加盟店番号が空");
    }

    @Test
    void deleteBatchDataDelegatesToRepository() {
        importer.deleteBatchData(7);

        verify(steraJcbSalesDetailRepository).deleteByBatchId(7);
    }

    private SteraTerminal activeTerminal(String tradeCode) {
        SteraTerminal terminal = new SteraTerminal();
        terminal.setTradeCode(tradeCode);
        terminal.setTerminalEndDate(null);
        return terminal;
    }

    private ImportBatch batch(int batchId, String employee) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(batchId);
        batch.setUpdateEmployee(employee);
        return batch;
    }
}
