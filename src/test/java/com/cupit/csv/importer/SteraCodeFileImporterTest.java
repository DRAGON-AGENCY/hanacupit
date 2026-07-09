package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.model.ImportBatch;
import com.cupit.model.SteraCodeSettlementDetail;
import com.cupit.model.SteraCodeSettlementSummary;
import com.cupit.model.SteraTerminal;
import com.cupit.repository.SteraCodeSettlementDetailRepository;
import com.cupit.repository.SteraCodeSettlementSummaryRepository;
import com.cupit.repository.SteraTerminalRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link SteraCodeFileImporter} のテスト。個別明細の取引コード解決、
 * 小計行（伝票番号99999・決済時間000000）の判別とサマリテーブルへの振り分け、
 * サブウォレット名の空→null 変換、部分登録、extractLookupKey／deleteBatchData を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraCodeFileImporterTest {

    private static final String HEADER =
            "\"ブランド\",\"端末識別番号\",\"伝票番号\",\"決済年月日\",\"決済時間\","
            + "\"1:売上2:返品\",\"決済金額\",\"手数料金額\",\"収納金額\",\"サブウォレット名\"";

    @Mock
    private SteraCodeSettlementDetailRepository settlementDetailRepository;

    @Mock
    private SteraCodeSettlementSummaryRepository settlementSummaryRepository;

    @Mock
    private SteraTerminalRepository steraTerminalRepository;

    private SteraCodeFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SteraCodeFileImporter(
                settlementDetailRepository, settlementSummaryRepository, steraTerminalRepository);
    }

    @Test
    void importsDetailRowsAndResolvesTradeCode() throws Exception {
        when(steraTerminalRepository.findByTerminalId("7113462036751"))
                .thenReturn(List.of(activeTerminal("01-001")));
        when(steraTerminalRepository.findByTerminalId("7113462036121"))
                .thenReturn(List.of(activeTerminal("01-002")));

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_code_valid.csv"), batch(20, "user001"));

        assertThat(result.getSuccessCount()).isEqualTo(2);
        ArgumentCaptor<SteraCodeSettlementDetail> captor =
                ArgumentCaptor.forClass(SteraCodeSettlementDetail.class);
        verify(settlementDetailRepository, times(2)).save(captor.capture());
        SteraCodeSettlementDetail first = captor.getAllValues().get(0);
        assertThat(first.getTradeCode()).isEqualTo("01-001");
        assertThat(first.getBrand()).isEqualTo("楽天ペイ");
        assertThat(first.getSettlementAmount()).isEqualTo(5000);
        assertThat(first.getSubWalletName()).isNull();
    }

    @Test
    void detectsSummaryRowAndStoresInSummaryTable() throws Exception {
        when(steraTerminalRepository.findByTerminalId("7113462036751"))
                .thenReturn(List.of(activeTerminal("01-001")));
        when(steraTerminalRepository.findByTerminalId("7113462036121"))
                .thenReturn(List.of(activeTerminal("01-002")));

        importer.importFile(CsvFiles.fromClasspath("stera_code_valid.csv"), batch(20, "user001"));

        ArgumentCaptor<SteraCodeSettlementSummary> captor =
                ArgumentCaptor.forClass(SteraCodeSettlementSummary.class);
        verify(settlementSummaryRepository, times(1)).save(captor.capture());
        SteraCodeSettlementSummary summary = captor.getValue();
        assertThat(summary.getBrand()).isEqualTo("楽天ペイ");
        assertThat(summary.getTransactionCount()).isEqualTo(2);
        assertThat(summary.getSettlementAmount()).isEqualTo(8465);
        assertThat(summary.getFeeAmount()).isEqualTo(100);
        assertThat(summary.getNetAmount()).isEqualTo(8365);
    }

    @Test
    void treatsRowAsDetailWhenOnlyOneFixedMarkerMatches() throws Exception {
        when(steraTerminalRepository.findByTerminalId("7113462036751"))
                .thenReturn(List.of(activeTerminal("01-001")));

        // 伝票番号は99999だが決済時間が000000でないため小計行とは判別されない
        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv", HEADER,
                        "\"楽天ペイ\",\"7113462036751\",\"99999\",\"20251101\",\"091102\",\"1\",\"5000\",\"\",\"\",\"\""),
                batch(20, "user001"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(settlementSummaryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsDetailRowWhenNoActiveTerminalExists() throws Exception {
        when(steraTerminalRepository.findByTerminalId("7113462036751"))
                .thenReturn(List.of());

        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv", HEADER,
                        "\"楽天ペイ\",\"7113462036751\",\"03447\",\"20251101\",\"091102\",\"1\",\"5000\",\"\",\"\",\"\""),
                batch(20, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("有効な端末情報");
    }

    @Test
    void skipsDetailRowWhenMultipleActiveTerminals() throws Exception {
        when(steraTerminalRepository.findByTerminalId("7113462036751"))
                .thenReturn(List.of(activeTerminal("01-001"), activeTerminal("09-999")));

        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv", HEADER,
                        "\"楽天ペイ\",\"7113462036751\",\"03447\",\"20251101\",\"091102\",\"1\",\"5000\",\"\",\"\",\"\""),
                batch(20, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("複数件存在");
    }

    @Test
    void skipsDetailRowWhenSettlementAmountIsNonNumeric() throws Exception {
        when(steraTerminalRepository.findByTerminalId("7113462036751"))
                .thenReturn(List.of(activeTerminal("01-001")));

        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv", HEADER,
                        "\"楽天ペイ\",\"7113462036751\",\"03447\",\"20251101\",\"091102\",\"1\",\"ABC\",\"\",\"\",\"\""),
                batch(20, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void reportsColumnCountErrorForShortRow() throws Exception {
        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv", HEADER, "\"楽天ペイ\",\"7113462036751\""),
                batch(20, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正");
    }

    @Test
    void skipsSummaryRowWhenAmountIsNonNumeric() throws Exception {
        ImportResult result = importer.importFile(
                CsvFiles.utf8Bom("x.csv", HEADER,
                        "\"楽天ペイ\",\"S68473628\",\"99999\",\"20251115\",\"000000\",\"2\",\"8465\",\"XX\",\"8365\",\"\""),
                batch(20, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        verify(settlementSummaryRepository, never()).save(org.mockito.ArgumentMatchers.any());
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void extractLookupKeyReturnsTerminalIdOfSecondRow() throws Exception {
        String key = importer.extractLookupKey(CsvFiles.fromClasspath("stera_code_valid.csv"));

        assertThat(key).isEqualTo("7113462036751");
    }

    @Test
    void extractLookupKeyThrowsWhenTerminalIdIsBlank() throws Exception {
        assertThatThrownBy(() -> importer.extractLookupKey(
                CsvFiles.utf8Bom("x.csv", HEADER, "\"楽天ペイ\",\"\"")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteBatchDataDeletesBothDetailAndSummary() {
        importer.deleteBatchData(9);

        verify(settlementDetailRepository).deleteByBatchId(9);
        verify(settlementSummaryRepository).deleteByBatchId(9);
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
