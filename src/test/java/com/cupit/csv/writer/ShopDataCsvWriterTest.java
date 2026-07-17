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
import com.cupit.csv.importer.ShopDataFileImporter;
import com.cupit.model.ImportBatch;
import com.cupit.model.ShopData;
import com.cupit.repository.ShopDataRepository;

/**
 * {@link ShopDataCsvWriter} のテスト。UTF-8 BOM付き出力、RFC4180準拠のクォート処理、
 * null値の空文字化、および {@link ShopDataFileImporter} による再取り込みとの
 * 往復整合性（ダウンロードしたファイルをそのまま再アップロードできること）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class ShopDataCsvWriterTest {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final ShopDataCsvWriter writer = new ShopDataCsvWriter();

    @Mock
    private ShopDataRepository shopDataRepository;

    private ShopDataFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new ShopDataFileImporter(shopDataRepository);
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
        assertThat(columns).hasSize(27);
        assertThat(columns.get(0)).isEqualTo("取引コード");
        assertThat(columns.get(27 - 1)).isEqualTo("（解約）手続状況");
    }

    @Test
    void writesNullFieldsAsEmpty() throws Exception {
        ShopData data = new ShopData();
        data.setTradeCode("01-001");

        byte[] csv = writer.writeCsv(List.of(data));
        List<String> row = secondLine(csv);

        assertThat(row.get(0)).isEqualTo("01-001");
        assertThat(row.get(1)).isEmpty();
    }

    @Test
    void formatsDateAsSlashSeparated() throws Exception {
        ShopData data = new ShopData();
        data.setTradeCode("01-001");
        data.setLinkageDate(java.time.LocalDate.of(2020, 1, 1));

        byte[] csv = writer.writeCsv(List.of(data));
        List<String> row = secondLine(csv);

        assertThat(row.get(19)).isEqualTo("2020/01/01");
    }

    @Test
    void quotesFieldsContainingCommaOrQuote() throws Exception {
        ShopData data = new ShopData();
        data.setTradeCode("01-001");
        data.setApplicationTypeFlag("備考、カンマと\"引用符\"を含む");

        byte[] csv = writer.writeCsv(List.of(data));
        List<String> row = secondLine(csv);

        assertThat(row.get(1)).isEqualTo("備考、カンマと\"引用符\"を含む");
    }

    @Test
    void roundTripsThroughFileImporter() throws Exception {
        ShopData original = new ShopData();
        original.setTradeCode("01-001");
        original.setApplicationTypeFlag("往復確認、カンマあり");

        byte[] csv = writer.writeCsv(List.of(original));
        MockMultipartFile uploadFile =
                new MockMultipartFile("file", "shop_data.csv", "text/csv", csv);
        when(shopDataRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        ImportResult result = importer.importFile(uploadFile, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<ShopData>> captor = ArgumentCaptor.forClass(List.class);
        verify(shopDataRepository).saveAll(captor.capture());
        ShopData reimported = captor.getValue().get(0);
        assertThat(reimported.getTradeCode()).isEqualTo("01-001");
        assertThat(reimported.getApplicationTypeFlag()).isEqualTo("往復確認、カンマあり");
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
