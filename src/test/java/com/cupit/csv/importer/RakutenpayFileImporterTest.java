package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.repository.PaygateMappingRepository;
import com.cupit.repository.RakutenPayTransactionRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link RakutenpayFileImporter} の extractAllLookupKeys（重複登録防止のための
 * 全識別キー収集）を検証する。同機能追加に伴い新設したテストクラスのため、
 * 対象は重複登録防止に関わる新規メソッドのみとする。
 */
@ExtendWith(MockitoExtension.class)
class RakutenpayFileImporterTest {

    @Mock
    private RakutenPayTransactionRepository rakutenPayTransactionRepository;

    @Mock
    private PaygateMappingRepository paygateMappingRepository;

    private RakutenpayFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new RakutenpayFileImporter(rakutenPayTransactionRepository, paygateMappingRepository);
    }

    @Test
    void extractAllLookupKeysReturnsDistinctStoreNosInOrder() throws Exception {
        // rakutenpay_duplicate_key.xlsx: 2・3行目は同一STORE_NO（RP0001）、
        // 4行目は別STORE_NO（RP0002）。
        List<String> keys = importer.extractAllLookupKeys(
                CsvFiles.fromClasspath("jftd", "rakutenpay_duplicate_key.xlsx"));

        assertThat(keys).containsExactly("RP0001", "RP0002");
    }
}
