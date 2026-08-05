package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.SmccMerchantNo;
import com.cupit.repository.SmccMerchantNoRepository;

/**
 * 加盟店番号データ CSV を解析して m_smcc_merchant_no に取引コード単位で洗い替え登録する
 * （取引コードが存在しなければ新規登録、存在すれば該当取引コードのレコードのみを
 * 削除してから登録する）。CSVに含まれない取引コードの既存データは保持する。
 * 1取引コードに複数行（複数加盟店番号等）が存在する運用のため、取引コード自体の重複は
 * 許容し、複数行分がまとめて同一取引コードで登録される。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行や、取引コード未入力の行、必須項目未入力の行を含む行はその行だけを
 * 登録せずスキップし、ファイルの最後まで処理を継続する
 * （データエラーによってファイル全体をロールバックすることはしない）。
 */
@Component
public class SmccMerchantNoFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 4;

    private static final String[] COLUMN_NAMES = {
        "取引コード", "SMCC加盟店番号", "種別", "届出支店コード",
    };

    private static final Set<Integer> REQUIRED_INDEXES = Set.of(1, 2, 3);

    /**
     * 各列のm_smcc_merchant_no上のVARCHAR桁数上限（CSVフォーマット仕様書に準拠）。
     */
    private static final int[] MAX_LENGTHS = {10, 10, 20, 9};

    private final SmccMerchantNoRepository smccMerchantNoRepository;

    public SmccMerchantNoFileImporter(SmccMerchantNoRepository smccMerchantNoRepository) {
        this.smccMerchantNoRepository = smccMerchantNoRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<SmccMerchantNo> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 0, errors);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add(new CsvValidationError(rowNum, "取引コード",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                parseDataRow(fields, rowNum, batch, records, errors);
            }
        }

        List<String> tradeCodes = records.stream().map(SmccMerchantNo::getTradeCode).distinct().toList();
        smccMerchantNoRepository.deleteByTradeCodeIn(tradeCodes);
        smccMerchantNoRepository.saveAll(records);
        int totalDataRows = rowNum - 1;
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    private void parseDataRow(
            List<String> fields, int rowNum, ImportBatch batch,
            List<SmccMerchantNo> records, List<CsvValidationError> errors) {
        String tradeCode = trim(fields.get(0));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "取引コード", "取引コードは必須です。"));
            return;
        }

        int errorCountBeforeRow = errors.size();
        for (int index : REQUIRED_INDEXES) {
            if (trim(fields.get(index)).isEmpty()) {
                errors.add(new CsvValidationError(rowNum, COLUMN_NAMES[index],
                        "取引コード「" + tradeCode + "」: " + COLUMN_NAMES[index] + "は必須です。"));
            }
        }
        for (int index = 0; index < EXPECTED_COLUMN_COUNT; index++) {
            int maxLength = MAX_LENGTHS[index];
            String value = trim(fields.get(index));
            if (maxLength > 0 && value.length() > maxLength) {
                errors.add(new CsvValidationError(rowNum, COLUMN_NAMES[index],
                        "取引コード「" + tradeCode + "」: " + COLUMN_NAMES[index] + "は" + maxLength
                        + "文字以内で入力してください（実際: " + value.length() + "文字）。"));
            }
        }
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータエラーがあるため登録しない
        }

        SmccMerchantNo record = new SmccMerchantNo();
        record.setTradeCode(tradeCode);
        record.setMerchantNo(trim(fields.get(1)));
        record.setType(trim(fields.get(2)));
        record.setBranchCode(trim(fields.get(3)));
        OffsetDateTime now = OffsetDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUpdatedUserId(batch.getUpdateEmployee());
        records.add(record);
    }

}
