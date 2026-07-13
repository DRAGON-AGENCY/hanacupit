package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.cupit.model.SmccMerchantNo;
import com.cupit.model.SteraCreditSalesDetail;
import com.cupit.repository.SmccMerchantNoRepository;
import com.cupit.repository.SteraCreditSalesDetailRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link SteraCreditFileImporter} のテスト。利用加盟店番号からの取引コード解決
 * （0件・複数件はスキップ）、任意列の空→null 変換、部分登録、
 * extractLookupKey／deleteBatchData を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraCreditFileImporterTest {

    private static final String HEADER =
            "利用加盟店番号,送付日,取扱区分,取扱区分２,利用会員番号,利用日,金額符号,請求金額,"
            + "利用元金額,承認番号,CAT(POS)端末番号,異動データ識別,屋号,ブランド名称,"
            + "端末処理通番,サマリ件数,ＲＷ－ＩＤ,代表加盟店番号";

    @Mock
    private SteraCreditSalesDetailRepository steraCreditSalesDetailRepository;

    @Mock
    private SmccMerchantNoRepository smccMerchantNoRepository;

    private SteraCreditFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SteraCreditFileImporter(
                steraCreditSalesDetailRepository, smccMerchantNoRepository);
    }

    @Test
    void importsValidFileAndResolvesTradeCode() throws Exception {
        when(smccMerchantNoRepository.findByMerchantNo("12348894"))
                .thenReturn(List.of(merchant("01-001")));
        when(smccMerchantNoRepository.findByMerchantNo("12348936"))
                .thenReturn(List.of(merchant("01-002")));

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_credit_valid.csv"), batch(30, "user001"));

        assertThat(result.getSuccessCount()).isEqualTo(2);
        ArgumentCaptor<SteraCreditSalesDetail> captor =
                ArgumentCaptor.forClass(SteraCreditSalesDetail.class);
        verify(steraCreditSalesDetailRepository, times(2)).save(captor.capture());
        SteraCreditSalesDetail first = captor.getAllValues().get(0);
        assertThat(first.getTradeCode()).isEqualTo("01-001");
        assertThat(first.getMerchantId()).isEqualTo("12348894");
        assertThat(first.getBillingAmount()).isEqualTo(27500);
        assertThat(first.getCardNumberMasked()).isEqualTo("4***-****-****-6426");
        assertThat(first.getRepresentativeMerchantId()).isEqualTo("68473628");
    }

    @Test
    void setsBlankOptionalColumnsToNull() throws Exception {
        when(smccMerchantNoRepository.findByMerchantNo("12348894"))
                .thenReturn(List.of(merchant("01-001")));
        when(smccMerchantNoRepository.findByMerchantNo("12348936"))
                .thenReturn(List.of(merchant("01-002")));

        importer.importFile(CsvFiles.fromClasspath("stera_credit_valid.csv"), batch(30, "user001"));

        ArgumentCaptor<SteraCreditSalesDetail> captor =
                ArgumentCaptor.forClass(SteraCreditSalesDetail.class);
        verify(steraCreditSalesDetailRepository, times(2)).save(captor.capture());
        SteraCreditSalesDetail second = captor.getAllValues().get(1);
        // 2行目（12348936）は利用会員番号・取扱区分２・サマリ件数が空欄
        assertThat(second.getCardNumberMasked()).isNull();
        assertThat(second.getTransactionType2()).isNull();
        assertThat(second.getSummaryCount()).isNull();
        assertThat(second.getReaderWriterId()).isEqualTo("JW10730724793");
    }

    @Test
    void skipsRowWhenMerchantNotFound() throws Exception {
        when(smccMerchantNoRepository.findByMerchantNo("12348894"))
                .thenReturn(List.of(merchant("01-001")));
        when(smccMerchantNoRepository.findByMerchantNo("99999999"))
                .thenReturn(List.of());

        ImportResult result = importer.importFile(
                CsvFiles.fromClasspath("stera_credit_partial.csv"), batch(30, "user001"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("存在しません");
    }

    @Test
    void skipsRowWhenMultipleMerchantsFound() throws Exception {
        when(smccMerchantNoRepository.findByMerchantNo("12348894"))
                .thenReturn(List.of(merchant("01-001"), merchant("09-999")));

        ImportResult result = importer.importFile(
                CsvFiles.ms932("x.csv", HEADER, validRow("12348894")), batch(30, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("複数件存在");
    }

    @Test
    void reportsColumnCountErrorForShortRow() throws Exception {
        ImportResult result = importer.importFile(
                CsvFiles.ms932("x.csv", HEADER, "12348894,20251103,１回払"),
                batch(30, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正");
    }

    @Test
    void skipsRowWhenBillingAmountIsNonNumeric() throws Exception {
        when(smccMerchantNoRepository.findByMerchantNo("12348894"))
                .thenReturn(List.of(merchant("01-001")));
        String row =
                "12348894,20251103,１回払,,,20251103,0,ABC,27500,0847146,"
                + "71134-620-36114,,店舗,ＶＭ,00050,,,68473628";

        ImportResult result = importer.importFile(
                CsvFiles.ms932("x.csv", HEADER, row), batch(30, "user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void extractLookupKeyReturnsMerchantIdOfSecondRow() throws Exception {
        String key = importer.extractLookupKey(CsvFiles.fromClasspath("stera_credit_valid.csv"));

        assertThat(key).isEqualTo("12348894");
    }

    @Test
    void extractLookupKeyThrowsWhenMerchantIdIsBlank() throws Exception {
        assertThatThrownBy(() -> importer.extractLookupKey(
                CsvFiles.ms932("x.csv", HEADER, ",20251103,１回払")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void extractAllLookupKeysReturnsDistinctMerchantIdsInOrder() throws Exception {
        List<String> keys = importer.extractAllLookupKeys(CsvFiles.fromClasspath("stera_credit_valid.csv"));

        assertThat(keys).containsExactly("12348894", "12348936");
    }

    @Test
    void deleteBatchDataDelegatesToRepository() {
        importer.deleteBatchData(3);

        verify(steraCreditSalesDetailRepository).deleteByBatchId(3);
    }

    private String validRow(String merchantId) {
        return merchantId + ",20251103,１回払,,4***-****-****-6426,20251103,0,27500,27500,"
                + "0847146,71134-620-36114,,店舗,ＶＭ,00050,,,68473628";
    }

    private SmccMerchantNo merchant(String tradeCode) {
        SmccMerchantNo merchant = new SmccMerchantNo();
        merchant.setTradeCode(tradeCode);
        return merchant;
    }

    private ImportBatch batch(int batchId, String employee) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(batchId);
        batch.setUpdateEmployee(employee);
        return batch;
    }
}
