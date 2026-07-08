package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * steraクレジット売上件別明細CSVのフォーマットを検証するクラス。
 * 文字コード：Shift_JISを想定するが、detectCharsetによりBOM付きUTF-8ファイルも
 * 自動検出する。区切り文字：カンマ、囲み文字：なし、ヘッダー行：あり（1行目）。
 * ヘッダー行の列名はチェックしない（列数のみを検証対象とする）。
 * ファイル名に「売上件別明細CSV」「売上件別明細データCSV」の表記揺れがあるため、
 * 拡張子とヘッダー列数のみで判定し、ファイル名自体はチェックしない。
 */
public class SteraCreditCsvFormatValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 18;
    private static final String BOM = "﻿";

    private static final String[] EXPECTED_HEADERS = {
        "利用加盟店番号", "送付日", "取扱区分", "取扱区分２", "利用会員番号", "利用日",
        "金額符号", "請求金額", "利用元金額", "承認番号", "CAT(POS)端末番号", "異動データ識別",
        "屋号", "ブランド名称", "端末処理通番", "サマリ件数", "ＲＷ－ＩＤ", "代表加盟店番号"
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

            if (headerLine.startsWith(BOM)) {
                headerLine = headerLine.substring(1);
            }
            headerLine = stripCarriageReturn(headerLine);
            List<String> headers = parseLine(headerLine);

            if (headers.size() != EXPECTED_COLUMN_COUNT) {
                addColumnCountError(result, 1, EXPECTED_COLUMN_COUNT, headers.size());
                result.markFatal();
                result.setTotalRowCount(0);
                return result;
            }

            int rowNumber = 1;
            String line;
            while ((line = reader.readLine()) != null && rowNumber <= getMaxRowsToValidate()) {
                rowNumber++;
                line = stripCarriageReturn(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    addColumnCountError(result, rowNumber, EXPECTED_COLUMN_COUNT, fields.size());
                    continue;
                }
                String billingAmount = fields.get(7).trim();
                if (!isInteger(billingAmount)) {
                    addNumericError(result, rowNumber, EXPECTED_HEADERS[7], billingAmount);
                }
                String originalAmount = fields.get(8).trim();
                if (!isInteger(originalAmount)) {
                    addNumericError(result, rowNumber, EXPECTED_HEADERS[8], originalAmount);
                }
            }
            result.setTotalRowCount(rowNumber - 1);
        }
        return result;
    }

    private String stripCarriageReturn(String line) {
        if (line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }
}
