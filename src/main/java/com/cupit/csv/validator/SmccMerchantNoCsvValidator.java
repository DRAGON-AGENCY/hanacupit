package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * 加盟店番号データ（m_smcc_merchant_no）CSVのフォーマットを検証するクラス。
 * 4列固定（取引コード＋3項目）、1行目は内容によらず常にヘッダー行として扱う
 * （列名の一致チェックは行わない）。1取引コードに複数行（複数加盟店番号等）が存在する
 * 運用のため、取引コード自体のCSV内重複は許容する。m_smcc_merchant_noのNOT NULL制約に
 * 合わせ、全項目を必須とする（{@link #REQUIRED_INDEXES}）。
 */
@Component
public class SmccMerchantNoCsvValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 4;

    private static final String[] COLUMN_NAMES = {
        "取引コード", "SMCC加盟店番号", "種別", "届出支店コード",
    };

    private static final Set<Integer> REQUIRED_INDEXES = Set.of(1, 2, 3);

    /**
     * 各列のm_smcc_merchant_no上のVARCHAR桁数上限（CSVフォーマット仕様書に準拠）。
     */
    private static final int[] MAX_LENGTHS = {10, 10, 20, 9};

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

            int rowNumber = 1;
            String line;
            while ((line = reader.readLine()) != null && rowNumber <= getMaxRowsToValidate()) {
                rowNumber++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    result.addError(new CsvValidationError(rowNumber, "取引コード",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                validateDataRow(result, rowNumber, fields);
            }
            result.setTotalRowCount(rowNumber - 1);
        }
        return result;
    }

    private void validateDataRow(CsvValidationResult result, int rowNumber, List<String> fields) {
        String tradeCode = fields.get(0).trim();
        if (tradeCode.isEmpty()) {
            result.addError(new CsvValidationError(rowNumber, "取引コード", "取引コードは必須です。"));
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
