package com.cupit.csv.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.importer.ImportResult;
import com.cupit.csv.importer.MemberInfoFileImporter;
import com.cupit.csv.importer.MemberInfoRecordSaver;
import com.cupit.csv.validator.PaymentCompanyFormatChecker;
import com.cupit.model.ImportBatch;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * {@link MemberMasterCsvWriter} のテスト。UTF-8 BOM付き出力、RFC4180準拠のクォート処理、
 * null値の空文字化、および {@link MemberInfoFileImporter} による再取り込みとの
 * 往復整合性（ダウンロードしたファイルをそのまま再アップロードできること）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class MemberMasterCsvWriterTest {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final MemberMasterCsvWriter writer = new MemberMasterCsvWriter();

    @Mock
    private MemberInfoRepository memberInfoRepository;

    @Mock
    private MemberInfoRecordSaver memberInfoRecordSaver;

    private MemberInfoFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new MemberInfoFileImporter(
                memberInfoRepository, memberInfoRecordSaver, new PaymentCompanyFormatChecker());
    }

    @Test
    void writesUtf8Bom() {
        byte[] csv = writer.writeCsv(List.of());

        assertThat(csv[0]).isEqualTo(UTF8_BOM[0]);
        assertThat(csv[1]).isEqualTo(UTF8_BOM[1]);
        assertThat(csv[2]).isEqualTo(UTF8_BOM[2]);
    }

    @Test
    void headerHas255Columns() throws Exception {
        byte[] csv = writer.writeCsv(List.of());
        String headerLine = firstLine(csv);

        List<String> columns = parseCsvLine(headerLine);
        assertThat(columns).hasSize(255);
        assertThat(columns.get(0)).isEqualTo("取引コード");
        assertThat(columns.get(254)).isEqualTo("花キューピットタウン参加区分");
    }

    @Test
    void writesNullFieldsAsEmpty() throws Exception {
        MemberInfo info = new MemberInfo();
        info.setTradeCode("01-001");

        byte[] csv = writer.writeCsv(List.of(info));
        List<String> row = secondLine(csv);

        assertThat(row.get(0)).isEqualTo("01-001");
        assertThat(row.get(1)).isEmpty();
    }

    @Test
    void formatsDateAsSlashSeparated() throws Exception {
        MemberInfo info = new MemberInfo();
        info.setTradeCode("01-001");
        info.setJoinDate(LocalDate.of(1960, 8, 16));

        byte[] csv = writer.writeCsv(List.of(info));
        List<String> row = secondLine(csv);

        assertThat(row.get(7)).isEqualTo("1960/08/16");
    }

    @Test
    void quotesFieldsContainingCommaOrQuote() throws Exception {
        MemberInfo info = new MemberInfo();
        info.setTradeCode("01-001");
        info.setRemarks("備考、カンマと\"引用符\"を含む");

        byte[] csv = writer.writeCsv(List.of(info));
        List<String> row = secondLine(csv);

        assertThat(row.get(53)).isEqualTo("備考、カンマと\"引用符\"を含む");
    }

    @Test
    void roundTripsThroughMemberInfoFileImporter() throws Exception {
        MemberInfo original = new MemberInfo();
        original.setTradeCode("01-001");
        original.setStoreName("赤坂生花店");
        original.setStoreNameKana("アカサカセイカテン");
        original.setJoinDate(LocalDate.of(1960, 8, 16));
        original.setMidCode((short) 1);
        original.setCapitalYen(3_000_000L);
        original.setRemarks("特記事項、あり");
        original.setAddrZip("0700032");
        original.setAddrPref("北海道");
        original.setAddrPrefKana("ホッカイドウ");
        original.setAddrCity("旭川市");
        original.setAddrCityKana("アサヒカワシ");
        original.setAddrTown("二条通");
        original.setAddrTownKana("ニジョウドオリ");
        original.setAddrBlock("6丁目右6号");
        original.setAddrBlockKana("ロクチョウメミギロクゴウ");
        original.setAddrTel("0166-22-4276");
        original.setCorpZip("0700032");
        original.setCorpPref("北海道");
        original.setCorpPrefKana("ホッカイドウ");
        original.setCorpCity("旭川市");
        original.setCorpCityKana("アサヒカワシ");
        original.setCorpTown("二条通");
        original.setCorpTownKana("ニジョウドオリ");
        original.setCorpBlock("6丁目右6号");
        original.setCorpBlockKana("ロクチョウメミギロクゴウ");
        original.setRepLastName("田中");
        original.setRepFirstName("一郎");
        original.setRepLastNameKana("タナカ");
        original.setRepFirstNameKana("イチロウ");
        original.setRepBirth("19000101");
        original.setHandlingItems("慶弔葉鉢");

        byte[] csv = writer.writeCsv(List.of(original));
        MockMultipartFile uploadFile =
                new MockMultipartFile("file", "member_master.csv", "text/csv", csv);
        when(memberInfoRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        ImportResult result = importer.importFile(uploadFile, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<MemberInfo> captor = ArgumentCaptor.forClass(MemberInfo.class);
        verify(memberInfoRecordSaver).save(captor.capture());
        MemberInfo reimported = captor.getValue();
        assertThat(reimported.getTradeCode()).isEqualTo("01-001");
        assertThat(reimported.getStoreName()).isEqualTo("赤坂生花店");
        assertThat(reimported.getJoinDate()).isEqualTo(LocalDate.of(1960, 8, 16));
        assertThat(reimported.getMidCode()).isEqualTo((short) 1);
        assertThat(reimported.getCapitalYen()).isEqualTo(3_000_000L);
        assertThat(reimported.getRemarks()).isEqualTo("特記事項、あり");
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
