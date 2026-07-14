package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.repository.PaygateMappingRepository;
import com.cupit.repository.TerminalMonthlyFeeRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link SumarejoFileImporter} の extractAllLookupKeys（重複登録防止のための
 * 全識別キー収集）を検証する。同機能追加に伴い新設したテストクラスのため、
 * 対象は重複登録防止に関わる新規メソッドのみとする。
 */
@ExtendWith(MockitoExtension.class)
class SumarejoFileImporterTest {

    @Mock
    private TerminalMonthlyFeeRepository terminalMonthlyFeeRepository;

    @Mock
    private PaygateMappingRepository paygateMappingRepository;

    private SumarejoFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SumarejoFileImporter(terminalMonthlyFeeRepository, paygateMappingRepository);
    }

    @Test
    void extractAllLookupKeysReturnsDistinctTerminalIdsInOrder() throws Exception {
        // sumarejo_duplicate_key.csv: 1・2行目は同一端末識別番号（9999999999999）、
        // 3行目は別端末識別番号（8888888888888）。
        List<String> keys = importer.extractAllLookupKeys(
                CsvFiles.fromClasspath("jftd", "sumarejo_duplicate_key.csv"));

        assertThat(keys).containsExactly("9999999999999", "8888888888888");
    }
}
