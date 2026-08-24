package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.validator.PaymentCompanyFormatChecker;
import com.cupit.model.ImportBatch;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * {@link MemberInfoFileImporter} が実際に m_member_info テーブルへ登録し、再読み込みで
 * 同じ値が取得できることを、モック化しない実リポジトリ・実DB接続で検証する。
 * {@link MemberInfoFileImporterTest}はRepositoryをモック化しており「saveメソッドに渡した
 * Javaオブジェクトの値が正しいか」までしか保証しないため、実際のINSERT・カラム
 * マッピング・型変換・255列分のNOT NULL制約が正しく行われることをここで別途確認する。
 * {@link MemberInfoRecordSaver#save(MemberInfo)}は本番では
 * {@code @Transactional(propagation = REQUIRES_NEW)}で独立したトランザクションとして
 * 即コミットされるが、ここではSpringのAOPプロキシを経由しないよう{@code new}で
 * インスタンス化しているためこのアノテーションは効かず、通常のメソッド呼び出しとして
 * {@code @DataJpaTest}のテスト用トランザクションに含まれる（＝テスト終了後に自動
 * ロールバックされ、開発DBへ永続的なデータは残らない）。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberInfoFileImporterDbIntegrationTest {

    private static final int COLUMN_COUNT = 255;
    private static final String TEST_TRADE_CODE = "99-997";

    @Autowired
    private MemberInfoRepository memberInfoRepository;

    @AfterEach
    void cleanUp() {
        memberInfoRepository.deleteById(TEST_TRADE_CODE);
    }

    @Test
    void importedRowIsPersistedAndReadableFromDatabase() throws Exception {
        MemberInfoFileImporter importer = newImporter();
        MockMultipartFile file = csvFile(String.join(",", validFields(TEST_TRADE_CODE)));

        ImportResult result = importer.importFile(file, batch("dbtest"));
        memberInfoRepository.flush();

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        Optional<MemberInfo> reloaded = memberInfoRepository.findById(TEST_TRADE_CODE);
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStoreName()).isEqualTo("赤坂生花店");
        assertThat(reloaded.get().getMidCode()).isEqualTo((short) 1);
        assertThat(reloaded.get().getJoinDate()).isEqualTo(LocalDate.of(1960, 8, 16));
        assertThat(reloaded.get().getCapitalYen()).isEqualTo(3_000_000L);
        assertThat(reloaded.get().getUpdateEmployee()).isEqualTo("dbtest");
        assertThat(reloaded.get().getCreateDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void preservesExistingCreateDateOnUpsertAgainstRealDatabase() throws Exception {
        LocalDate originalCreateDate = LocalDate.of(2020, 4, 1);
        MemberInfo existing = new MemberInfo();
        existing.setTradeCode(TEST_TRADE_CODE);
        existing.setCreateDate(originalCreateDate);
        existing.setStoreName("旧店舗名");
        memberInfoRepository.saveAndFlush(existing);

        MemberInfoFileImporter importer = newImporter();
        String[] fields = validFields(TEST_TRADE_CODE);
        fields[13] = "新店舗名";
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("dbtest"));
        memberInfoRepository.flush();

        assertThat(result.hasErrors()).isFalse();

        MemberInfo reloaded = memberInfoRepository.findById(TEST_TRADE_CODE).orElseThrow();
        assertThat(reloaded.getStoreName()).isEqualTo("新店舗名");
        assertThat(reloaded.getCreateDate()).isEqualTo(originalCreateDate);
        assertThat(reloaded.getUpdatedDate()).isEqualTo(LocalDate.now());
    }

    private MemberInfoFileImporter newImporter() {
        return new MemberInfoFileImporter(
                memberInfoRepository,
                new MemberInfoRecordSaver(memberInfoRepository),
                new PaymentCompanyFormatChecker());
    }

    private ImportBatch batch(String employee) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(1);
        batch.setUpdateEmployee(employee);
        return batch;
    }

    private String[] validFields(String tradeCode) {
        String[] f = new String[COLUMN_COUNT];
        Arrays.fill(f, "");
        f[0] = tradeCode;
        f[5] = "1";
        f[7] = "1960/08/16";
        f[13] = "赤坂生花店";
        f[14] = "アカサカセイカテン";
        f[20] = "0700032";
        f[21] = "北海道";
        f[22] = "ホッカイドウ";
        f[23] = "旭川市";
        f[24] = "アサヒカワシ";
        f[25] = "二条通";
        f[26] = "ニジョウドオリ";
        f[27] = "6丁目右6号";
        f[28] = "ロクチョウメミギロクゴウ";
        f[31] = "0166-22-4276";
        f[41] = "慶弔葉鉢";
        f[62] = "0700032";
        f[63] = "北海道";
        f[64] = "ホッカイドウ";
        f[65] = "旭川市";
        f[66] = "アサヒカワシ";
        f[67] = "二条通";
        f[68] = "ニジョウドオリ";
        f[69] = "6丁目右6号";
        f[70] = "ロクチョウメミギロクゴウ";
        f[73] = "タナカ";
        f[74] = "イチロウ";
        f[75] = "田中";
        f[76] = "一郎";
        f[77] = "19000101";
        f[93] = "3000000";
        return f;
    }

    private MockMultipartFile csvFile(String... dataLines) {
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        for (String line : dataLines) {
            sb.append(line).append("\r\n");
        }
        return new MockMultipartFile(
                "file", "member_master.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

}
