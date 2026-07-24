package com.cupit.csv.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.importer.ImportResult;
import com.cupit.csv.importer.SteraStoreFileImporter;
import com.cupit.model.ImportBatch;
import com.cupit.model.SteraStore;
import com.cupit.repository.SteraStoreRepository;

/**
 * {@link SteraStoreCsvWriter} のテスト。UTF-8 BOM付き出力、RFC4180準拠のクォート処理、
 * null値の空文字化、および {@link SteraStoreFileImporter} による再取り込みとの
 * 往復整合性（ダウンロードしたファイルをそのまま再アップロードできること）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraStoreCsvWriterTest {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final SteraStoreCsvWriter writer = new SteraStoreCsvWriter();

    @Mock
    private SteraStoreRepository steraStoreRepository;

    private SteraStoreFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SteraStoreFileImporter(steraStoreRepository);
    }

    @Test
    void writesUtf8Bom() {
        byte[] csv = writer.writeCsv(List.of());

        assertThat(csv[0]).isEqualTo(UTF8_BOM[0]);
        assertThat(csv[1]).isEqualTo(UTF8_BOM[1]);
        assertThat(csv[2]).isEqualTo(UTF8_BOM[2]);
    }

    @Test
    void headerHasExpectedColumns() throws Exception {
        byte[] csv = writer.writeCsv(List.of());
        String headerLine = firstLine(csv);

        List<String> columns = parseCsvLine(headerLine);
        assertThat(columns).hasSize(30);
        assertThat(columns.get(0)).isEqualTo("取引コード");
        assertThat(columns.get(29)).isEqualTo("備考");
    }

    @Test
    void writesNullFieldsAsEmpty() throws Exception {
        SteraStore data = new SteraStore();
        data.setTradeCode("01-001");

        byte[] csv = writer.writeCsv(List.of(data));
        List<String> row = secondLine(csv);

        assertThat(row.get(0)).isEqualTo("01-001");
        assertThat(row.get(1)).isEmpty();
    }

    @Test
    void formatsDateAsSlashSeparated() throws Exception {
        SteraStore data = new SteraStore();
        data.setTradeCode("01-001");
        data.setJcbStartDate(java.time.LocalDate.of(2020, 1, 1));

        byte[] csv = writer.writeCsv(List.of(data));
        List<String> row = secondLine(csv);

        assertThat(row.get(26)).isEqualTo("2020/01/01");
    }

    @Test
    void quotesFieldsContainingCommaOrQuote() throws Exception {
        SteraStore data = new SteraStore();
        data.setTradeCode("01-001");
        data.setRemarks("備考、カンマと\"引用符\"を含む");

        byte[] csv = writer.writeCsv(List.of(data));
        List<String> row = secondLine(csv);

        assertThat(row.get(29)).isEqualTo("備考、カンマと\"引用符\"を含む");
    }

    @Test
    void roundTripsThroughFileImporter() throws Exception {
        SteraStore original = new SteraStore();
        original.setTradeCode("01-001");
        original.setTransitCompany("Suica");
        original.setEdyId("12345678");
        original.setBranchCode("01-001000");
        original.setStoreName("テスト店舗");
        original.setStoreNameKana("テストテンポ");
        original.setStoreNameEn("TEST STORE");
        original.setStoreZip("1000001");
        original.setStoreAddress("東京都");
        original.setStoreAddressKana("トウキョウト");
        original.setStoreTel("03-1234-5678");
        original.setEmail("test@example.com");
        original.setBankName("テスト銀行");
        original.setBankCode("0001");
        original.setBranchName("テスト支店");
        original.setBankBranchCode("001");
        original.setAccountType("1:普通");
        original.setAccountNo("1234567");
        original.setAccountHolderKana("テスト");
        original.setJcbStatus("済");
        original.setDPointStatus("未");

        byte[] csv = writer.writeCsv(List.of(original));
        MockMultipartFile uploadFile =
                new MockMultipartFile("file", "stera_store.csv", "text/csv", csv);
        when(steraStoreRepository.findByTradeCodeIn(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        ImportResult result = importer.importFile(uploadFile, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<SteraStore>> captor = ArgumentCaptor.forClass(List.class);
        verify(steraStoreRepository).saveAll(captor.capture());
        SteraStore reimported = captor.getValue().get(0);
        assertThat(reimported.getTradeCode()).isEqualTo("01-001");
        assertThat(reimported.getStoreName()).isEqualTo("テスト店舗");
    }

    private ImportBatch batch(String employee) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(1);
        batch.setUpdateEmployee(employee);
        return batch;
    }

    private String firstLine(byte[] csv) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(csv, UTF8_BOM.length, csv.length - UTF8_BOM.length),
                StandardCharsets.UTF_8))) {
            return reader.readLine();
        }
    }

    private List<String> secondLine(byte[] csv) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(csv, UTF8_BOM.length, csv.length - UTF8_BOM.length),
                StandardCharsets.UTF_8))) {
            reader.readLine();
            return parseCsvLine(reader.readLine());
        }
    }

    /** RFC4180準拠の簡易CSVパーサー（テスト検証専用）。 */
    private List<String> parseCsvLine(String line) {
        List<String> fields = new java.util.ArrayList<>();
        int i = 0;
        while (i <= line.length()) {
            if (i < line.length() && line.charAt(i) == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < line.length()) {
                    if (line.charAt(i) == '"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            sb.append('"');
                            i += 2;
                        } else {
                            i++;
                            break;
                        }
                    } else {
                        sb.append(line.charAt(i++));
                    }
                }
                fields.add(sb.toString());
                if (i < line.length() && line.charAt(i) == ',') {
                    i++;
                } else {
                    break;
                }
            } else {
                int start = i;
                while (i < line.length() && line.charAt(i) != ',') {
                    i++;
                }
                fields.add(line.substring(start, i));
                if (i < line.length()) {
                    i++;
                } else {
                    break;
                }
            }
        }
        return fields;
    }

}
