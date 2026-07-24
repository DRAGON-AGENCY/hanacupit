package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.model.ImportBatch;
import com.cupit.model.SmccMerchantNo;
import com.cupit.repository.SmccMerchantNoRepository;

/**
 * {@link SmccMerchantNoFileImporter} が実際に m_smcc_merchant_no テーブルへ登録し、
 * 再読み込みで同じ値が取得できることを、モック化しない実リポジトリ・実DB接続で検証する。
 * {@link SmccMerchantNoFileImporterTest}はRepositoryをモック化しており「saveAll()に渡した
 * Javaオブジェクトの値が正しいか」までしか保証しないため、実際のINSERT・カラム
 * マッピング・型変換・NOT NULL制約が正しく行われることをここで別途確認する。
 * {@code @DataJpaTest}は各テストメソッドをトランザクションで囲み終了後に自動
 * ロールバックするため、開発DBへ永続的なデータは残らない。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SmccMerchantNoFileImporterDbIntegrationTest {

    private static final int COLUMN_COUNT = 4;
    private static final String TEST_TRADE_CODE = "99-993";
    private static final String TEST_MERCHANT_NO = "DBTEST01";

    @Autowired
    private SmccMerchantNoRepository smccMerchantNoRepository;

    @Test
    void importedRowIsPersistedAndReadableFromDatabase() throws Exception {
        SmccMerchantNoFileImporter importer =
                new SmccMerchantNoFileImporter(smccMerchantNoRepository);
        MockMultipartFile file = csvFile(String.join(",", validFields(TEST_TRADE_CODE)));
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(1);
        batch.setUpdateEmployee("dbtest");

        ImportResult result = importer.importFile(file, batch);
        smccMerchantNoRepository.flush();

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        List<SmccMerchantNo> reloaded = smccMerchantNoRepository.findByMerchantNo(TEST_MERCHANT_NO);
        assertThat(reloaded).hasSize(1);
        assertThat(reloaded.get(0).getTradeCode()).isEqualTo(TEST_TRADE_CODE);
        assertThat(reloaded.get(0).getUpdatedUserId()).isEqualTo("dbtest");
    }

    private String[] validFields(String tradeCode) {
        String[] f = new String[COLUMN_COUNT];
        Arrays.fill(f, "");
        f[0] = tradeCode;
        f[1] = TEST_MERCHANT_NO;
        f[2] = "クレジット";
        f[3] = "01-001000";
        return f;
    }

    private MockMultipartFile csvFile(String... dataLines) {
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        for (String line : dataLines) {
            sb.append(line).append("\r\n");
        }
        return new MockMultipartFile(
                "file", "smcc_merchant_no.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

}
