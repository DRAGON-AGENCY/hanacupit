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
import com.cupit.model.SteraTerminal;
import com.cupit.repository.SteraTerminalRepository;

/**
 * 端末データ CSV を解析して m_stera_terminal に取引コード単位で洗い替え登録する
 * （取引コードが存在しなければ新規登録、存在すれば該当取引コードのレコードのみを
 * 削除してから登録する）。CSVに含まれない取引コードの既存データは保持する。
 * 1取引コードに複数行（複数端末等）が存在する運用のため、取引コード自体の重複は
 * 許容し、複数行分がまとめて同一取引コードで登録される。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行や、取引コード未入力の行、必須項目未入力の行、日付変換に失敗した項目を含む行は
 * その行だけを登録せずスキップし、ファイルの最後まで処理を継続する
 * （データエラーによってファイル全体をロールバックすることはしない）。
 */
@Component
public class SteraTerminalFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 7;

    private static final String[] COLUMN_NAMES = {
        "取引コード", "端末識別番号", "JCB加盟店番号", "届出支店コード",
        "端末利用ステータス", "端末利用開始日", "端末利用終了日",
    };

    private static final Set<Integer> REQUIRED_INDEXES = Set.of(1, 3, 4, 5);

    private final SteraTerminalRepository steraTerminalRepository;

    public SteraTerminalFileImporter(SteraTerminalRepository steraTerminalRepository) {
        this.steraTerminalRepository = steraTerminalRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<SteraTerminal> records = new ArrayList<>();
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

        List<String> tradeCodes = records.stream().map(SteraTerminal::getTradeCode).distinct().toList();
        steraTerminalRepository.deleteByTradeCodeIn(tradeCodes);
        steraTerminalRepository.saveAll(records);
        int totalDataRows = rowNum - 1;
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    private void parseDataRow(
            List<String> fields, int rowNum, ImportBatch batch,
            List<SteraTerminal> records, List<CsvValidationError> errors) {
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

        SteraTerminal record = new SteraTerminal();
        record.setTradeCode(tradeCode);
        record.setTerminalId(trim(fields.get(1)));
        record.setJcbMerchantNo(blankToNull(fields.get(2)));
        record.setBranchCode(trim(fields.get(3)));
        record.setTerminalStatus(trim(fields.get(4)));
        record.setTerminalStartDate(
                parseDateSlashChecked(fields.get(5), rowNum, "端末利用開始日", errors));
        record.setTerminalEndDate(
                parseDateSlashChecked(fields.get(6), rowNum, "端末利用終了日", errors));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータエラーがあるため登録しない
        }
        OffsetDateTime now = OffsetDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setUpdatedUserId(batch.getUpdateEmployee());
        records.add(record);
    }

    private String blankToNull(String s) {
        String trimmed = trim(s);
        return (trimmed == null || trimmed.isEmpty()) ? null : trimmed;
    }

}
