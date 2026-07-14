package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.repository.NetstarSalesSummaryRepository;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link NetstarFileImporter} の extractAllLookupKeys（重複登録防止のための
 * 全識別キー収集）を検証する。同機能追加に伴い新設したテストクラスのため、
 * 対象は重複登録防止に関わる新規メソッドのみとする。
 */
@ExtendWith(MockitoExtension.class)
class NetstarFileImporterTest {

    @Mock
    private NetstarSalesSummaryRepository netstarSalesSummaryRepository;

    @Mock
    private PaygateMappingRepository paygateMappingRepository;

    private NetstarFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new NetstarFileImporter(netstarSalesSummaryRepository, paygateMappingRepository);
    }

    @Test
    void extractAllLookupKeysReturnsDistinctStoreCodesInOrder() throws Exception {
        // netstar_duplicate_key.xlsx: 8・9行目は同一店舗コード（ST0001）、
        // 10行目は別店舗コード（ST0002）。
        List<String> keys = importer.extractAllLookupKeys(
                CsvFiles.fromClasspath("jftd", "netstar_duplicate_key.xlsx"));

        assertThat(keys).containsExactly("ST0001", "ST0002");
    }
}
