package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * 店舗データ（m_stera_store）CSVのフォーマットを検証するクラス。
 * 30列固定（取引コード＋29項目）、1行目は内容によらず常にヘッダー行として扱う
 * （列名の一致チェックは行わない）。1取引コード=1行、CSV内で取引コードが重複する場合は
 * どちらの行が正しいか判断できないため、該当する取引コードの行を（先着1件目も含めて）
 * 全てエラーとする。m_stera_storeのNOT NULL制約に合わせ、取引コード以外にも
 * 多数の項目を必須とする（{@link #REQUIRED_INDEXES}）。
 */
@Component
public class SteraStoreCsvValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 30;

    private static final String[] COLUMN_NAMES = {
        "取引コード", "交通系社局", "Edy ID", "dポイント加盟店コード", "dポイント店舗コード",
        "dポイント支部コード", "届出支店コード", "会員種別", "店舗名", "店舗名カナ",
        "店舗名（英字）", "店舗郵便番号", "店舗住所（漢字）", "店舗住所（カナ）", "店舗電話番号",
        "メールアドレス", "緯度", "経度", "金融機関名", "金融機関コード",
        "支店名", "支店コード", "口座種別", "口座番号", "口座名義（カナ）",
        "JCB利用状況", "JCB利用開始日", "dポイント利用状況", "dポイント利用開始日", "備考",
    };

    private static final Set<Integer> REQUIRED_INDEXES = Set.of(
            1, 2, 6, 8, 9, 10, 11, 12, 13, 14, 15, 18, 19, 20, 21, 22, 23, 24, 25, 27);

    /**
     * 各列のm_stera_store上のVARCHAR桁数上限（CSVフォーマット仕様書に準拠）。
     * DATE・NUMERIC・TEXT型の列（緯度・経度・JCB/dポイント利用開始日・備考）は
     * 文字数での上限チェックが適用できない／不要なため0（チェック対象外）とする。
     */
    private static final int[] MAX_LENGTHS = {
        10, 10, 8, 6, 13, 6, 9, 10, 50, 80,
        80, 7, 100, 150, 20, 100, 0, 0, 30, 4,
        20, 3, 4, 7, 80, 1, 0, 1, 0, 0,
    };

    @Override
    public CsvValidationResult validate(MultipartFile file) throws IOException {
        CsvValidationResult result = new CsvValidationResult();

        String ext = getExtension(file.getOriginalFilename());
        if (!"csv".equals(ext)) {
            result.addError(new CsvValidationError(
                    0, "", "ファイルの拡張子が不正です。期待: .csv、実際: ." + ext));
            result.markFatal();
            return result;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.addError(new CsvValidationError(1, "", "ファイルが空です。"));
                result.markFatal();
                return result;
            }
            if (headerLine.startsWith("﻿")) {
                headerLine = headerLine.substring(1);
            }
            headerLine = stripCr(headerLine);
            List<String> headerFields = parseLine(headerLine);

            if (headerFields.size() != EXPECTED_COLUMN_COUNT) {
                addColumnCountError(result, 1, EXPECTED_COLUMN_COUNT, headerFields.size());
                result.markFatal();
                result.setTotalRowCount(0);
                return result;
            }

            List<String> dataLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null && dataLines.size() < getMaxRowsToValidate()) {
                dataLines.add(stripCr(line));
            }

            Map<String, Integer> tradeCodeCounts = countTradeCodes(dataLines);

            int rowNumber = 1;
            for (String dataLine : dataLines) {
                rowNumber++;
                if (dataLine.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(dataLine);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    result.addError(new CsvValidationError(rowNumber, "取引コード",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                validateDataRow(result, rowNumber, fields, tradeCodeCounts);
            }
            result.setTotalRowCount(rowNumber - 1);
        }
        return result;
    }

    /**
     * データ行を1回走査し、取引コードごとの出現回数を数える。列数が不正な行は
     * 取引コード自体を安全に取得できない可能性があるため集計対象から除く
     * （その行は別途「列数が不正です」エラーになる）。
     */
    private Map<String, Integer> countTradeCodes(List<String> dataLines) {
        Map<String, Integer> counts = new HashMap<>();
        for (String dataLine : dataLines) {
            if (dataLine.isBlank()) {
                continue;
            }
            List<String> fields = parseLine(dataLine);
            if (fields.size() != EXPECTED_COLUMN_COUNT) {
                continue;
            }
            String tradeCode = fields.get(0).trim();
            if (!tradeCode.isEmpty()) {
                counts.merge(tradeCode, 1, Integer::sum);
            }
        }
        return counts;
    }

    private void validateDataRow(
            CsvValidationResult result, int rowNumber, List<String> fields,
            Map<String, Integer> tradeCodeCounts) {
        String tradeCode = fields.get(0).trim();
        if (tradeCode.isEmpty()) {
            result.addError(new CsvValidationError(rowNumber, "取引コード", "取引コードは必須です。"));
            return;
        }
        if (tradeCodeCounts.getOrDefault(tradeCode, 0) > 1) {
            result.addError(new CsvValidationError(rowNumber, "取引コード",
                    "取引コード「" + tradeCode + "」がCSV内で重複しています。"));
            return;
        }
        for (int index = 0; index < EXPECTED_COLUMN_COUNT; index++) {
            String value = fields.get(index).trim();
            if (REQUIRED_INDEXES.contains(index) && value.isEmpty()) {
                result.addError(new CsvValidationError(rowNumber, COLUMN_NAMES[index],
                        "取引コード「" + tradeCode + "」: " + COLUMN_NAMES[index] + "は必須です。"));
                continue;
            }
            int maxLength = MAX_LENGTHS[index];
            if (maxLength > 0 && value.length() > maxLength) {
                result.addError(new CsvValidationError(rowNumber, COLUMN_NAMES[index],
                        "取引コード「" + tradeCode + "」: " + COLUMN_NAMES[index] + "は" + maxLength
                        + "文字以内で入力してください（実際: " + value.length() + "文字）。"));
            }
        }
    }

    private String stripCr(String line) {
        if (line != null && line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

}
