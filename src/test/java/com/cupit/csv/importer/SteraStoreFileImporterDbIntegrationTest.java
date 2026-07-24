package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.model.ImportBatch;
import com.cupit.model.SteraStore;
import com.cupit.repository.SteraStoreRepository;

/**
 * {@link SteraStoreFileImporter} が実際に m_stera_store テーブルへ登録し、再読み込みで
 * 同じ値が取得できることを、モック化しない実リポジトリ・実DB接続で検証する。
 * {@link SteraStoreFileImporterTest}はRepositoryをモック化しており「saveAll()に渡した
 * Javaオブジェクトの値が正しいか」までしか保証しないため、実際のINSERT・カラム
 * マッピング・型変換・NOT NULL制約が正しく行われることをここで別途確認する。
 * {@code @DataJpaTest}は各テストメソッドをトランザクションで囲み終了後に自動
 * ロールバックするため、開発DBへ永続的なデータは残らない
 * （record_noは自動採番のため、ロールバック前提でクリーンアップは行わない）。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SteraStoreFileImporterDbIntegrationTest {

    private static final int COLUMN_COUNT = 30;
    private static final String TEST_TRADE_CODE = "99-991";

    @Autowired
    private SteraStoreRepository steraStoreRepository;

    @Test
    void importedRowIsPersistedAndReadableFromDatabase() throws Exception {
        SteraStoreFileImporter importer = new SteraStoreFileImporter(steraStoreRepository);
        MockMultipartFile file = csvFile(String.join(",", validFields(TEST_TRADE_CODE)));
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(1);
        batch.setUpdateEmployee("dbtest");

        ImportResult result = importer.importFile(file, batch);
        steraStoreRepository.flush();

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        Optional<SteraStore> reloaded = steraStoreRepository.findByTradeCode(TEST_TRADE_CODE);
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStoreName()).isEqualTo("テスト店舗");
        assertThat(reloaded.get().getUpdatedUserId()).isEqualTo("dbtest");
        assertThat(reloaded.get().getCreatedAt()).isNotNull();
    }

    private String[] validFields(String tradeCode) {
        String[] f = new String[COLUMN_COUNT];
        Arrays.fill(f, "");
        f[0] = tradeCode;
        f[1] = "Suica";
        f[2] = "12345678";
        f[6] = "01-001000";
        f[8] = "テスト店舗";
        f[9] = "テストテンポ";
        f[10] = "TEST STORE";
        f[11] = "1000001";
        f[12] = "東京都";
        f[13] = "トウキョウト";
        f[14] = "03-1234-5678";
        f[15] = "test@example.com";
        f[18] = "テスト銀行";
        f[19] = "0001";
        f[20] = "テスト支店";
        f[21] = "001";
        f[22] = "1:普通";
        f[23] = "1234567";
        f[24] = "テスト";
        f[25] = "済";
        f[27] = "未";
        return f;
    }

    private MockMultipartFile csvFile(String... dataLines) {
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        for (String line : dataLines) {
            sb.append(line).append("\r\n");
        }
        return new MockMultipartFile(
                "file", "stera_store.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

}
