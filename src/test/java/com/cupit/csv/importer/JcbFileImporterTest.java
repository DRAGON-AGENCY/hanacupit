package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.repository.JcbSalesDetailRepository;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link JcbFileImporter} の extractAllLookupKeys（重複登録防止のための
 * 全識別キー収集）を検証する。同機能追加に伴い新設したテストクラスのため、
 * 対象は重複登録防止に関わる新規メソッドのみとする。
 */
@ExtendWith(MockitoExtension.class)
class JcbFileImporterTest {

    @Mock
    private JcbSalesDetailRepository jcbSalesDetailRepository;

    @Mock
    private PaygateMappingRepository paygateMappingRepository;

    private JcbFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new JcbFileImporter(jcbSalesDetailRepository, paygateMappingRepository);
    }

    @Test
    void extractAllLookupKeysReturnsDistinctMerchantNosInOrder() throws Exception {
        // jcb_duplicate_key.csv: 1・2行目は同一加盟店番号（11111111111111）、
        // 3行目は別加盟店番号（22222222222222）。
        List<String> keys = importer.extractAllLookupKeys(
                CsvFiles.fromClasspath("jftd", "jcb_duplicate_key.csv"));

        assertThat(keys).containsExactly("11111111111111", "22222222222222");
    }
}
