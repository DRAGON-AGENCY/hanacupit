package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.MerchantNumberData;
import com.cupit.repository.MerchantNumberDataRepository;

/**
 * 加盟店番号データ CSV を解析して m_merchant_number_data に取引コード単位で洗い替え登録する
 * （取引コードが存在しなければ新規登録、存在すれば該当取引コードのレコードのみを
 * 削除してから登録する）。CSVに含まれない取引コードの既存データは保持する。
 * 1取引コードに複数行（複数端末等）が存在する運用のため、取引コード自体の重複は
 * 許容し、複数行分がまとめて同一取引コードで登録される。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行や、取引コード未入力の行、日付・数値変換に失敗した項目を含む行はその行だけを
 * 登録せずスキップし、ファイルの最後まで処理を継続する
 * （データエラーによってファイル全体をロールバックすることはしない）。取引コード以外の
 * 全項目は任意とする。
 */
@Component
public class MerchantNumberDataFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 26;

    private final MerchantNumberDataRepository merchantNumberDataRepository;

    public MerchantNumberDataFileImporter(MerchantNumberDataRepository merchantNumberDataRepository) {
        this.merchantNumberDataRepository = merchantNumberDataRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<MerchantNumberData> records = new ArrayList<>();
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

        Set<String> tradeCodes = records.stream()
                .map(MerchantNumberData::getTradeCode)
                .collect(Collectors.toSet());
        merchantNumberDataRepository.deleteByTradeCodeIn(tradeCodes);
        merchantNumberDataRepository.saveAll(records);
        int totalDataRows = rowNum - 1;
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    private void parseDataRow(
            List<String> fields, int rowNum, ImportBatch batch,
            List<MerchantNumberData> records, List<CsvValidationError> errors) {
        String tradeCode = trim(fields.get(0));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "取引コード", "取引コードは必須です。"));
            return;
        }

        int errorCountBeforeRow = errors.size();
        MerchantNumberData record = new MerchantNumberData();
        record.setTradeCode(tradeCode);
        record.setTerminalCount(parseIntegerChecked(fields.get(1), rowNum, "端末台数", errors));
        record.setLineType(trim(fields.get(2)));
        record.setStoreContactName(trim(fields.get(3)));
        record.setStoreContactTel(trim(fields.get(4)));
        record.setPosConnectionFlag(trim(fields.get(5)));
        record.setPosMakerName(trim(fields.get(6)));
        record.setPosVendorContactName(trim(fields.get(7)));
        record.setPosVendorContactTel(trim(fields.get(8)));
        record.setDPointEnabledFlag(trim(fields.get(9)));
        record.setDPointMerchantCode(trim(fields.get(10)));
        record.setDPointStoreCode(trim(fields.get(11)));
        record.setDPointBranchCode(trim(fields.get(12)));
        record.setVisaMasterMerchantNumber(trim(fields.get(13)));
        record.setNanacoMerchantNumber(trim(fields.get(14)));
        record.setIdMerchantNumber(trim(fields.get(15)));
        record.setTransitMerchantNumber(trim(fields.get(16)));
        record.setUnionpayMerchantNumber(trim(fields.get(17)));
        record.setWaonMerchantNumber(trim(fields.get(18)));
        record.setEdyMerchantNumber(trim(fields.get(19)));
        record.setNfcMerchantNumber(trim(fields.get(20)));
        record.setTransitOperator(trim(fields.get(21)));
        record.setEdyId(trim(fields.get(22)));
        record.setSteraTerminalNumber(trim(fields.get(23)));
        record.setJcbConnectionFlag(trim(fields.get(24)));
        record.setSmartCodeConnectionFlag(trim(fields.get(25)));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータ変換エラーがあるため登録しない
        }
        record.setRegisteredDate(LocalDate.now());
        record.setUpdatedBy(batch.getUpdateEmployee());
        records.add(record);
    }

}
