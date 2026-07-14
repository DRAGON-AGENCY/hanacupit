package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.repository.PaygateMappingRepository;
import com.cupit.repository.VisaMasterStoreHeaderRepository;
import com.cupit.repository.VisaMasterTransactionRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link JushinSbiFileImporter} の extractAllLookupKeys（重複登録防止のための
 * 全識別キー収集）を検証する。同機能追加に伴い新設したテストクラスのため、
 * 対象は重複登録防止に関わる新規メソッドのみとする。
 */
@ExtendWith(MockitoExtension.class)
class JushinSbiFileImporterTest {

    @Mock
    private VisaMasterStoreHeaderRepository headerRepository;

    @Mock
    private VisaMasterTransactionRepository transactionRepository;

    @Mock
    private PaygateMappingRepository paygateMappingRepository;

    private JushinSbiFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new JushinSbiFileImporter(
                headerRepository, transactionRepository, paygateMappingRepository);
    }

    @Test
    void extractAllLookupKeysReturnsDistinctMerchantIdsFromHeaderRecordsOnly() throws Exception {
        // jushinsbi_duplicate_key.csv: 区分1が3件（1・2行目は同一加盟店ID
        // 1111111111、3行目は別加盟店ID 2222222222）。区分2は含まれないファイルだが、
        // 区分1のみからキーを収集する仕様のため、区分2の有無は結果に影響しない。
        List<String> keys = importer.extractAllLookupKeys(
                CsvFiles.fromClasspath("jftd", "jushinsbi_duplicate_key.csv"));

        assertThat(keys).containsExactly("1111111111", "2222222222");
    }
}
