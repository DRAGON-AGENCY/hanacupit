package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * stera code精算明細CSVのフォーマットを検証するクラス。
 * 文字コード：UTF-8（BOM付き）を想定するが、detectCharsetによりBOMなしファイルは
 * MS932として自動検出する。区切り文字：カンマ、囲み文字：なし、ヘッダー行：あり（1行目）。
 * ヘッダー行の列名はチェックしない（列数のみを検証対象とする）。
 * ブランドごとの明細ブロック末尾にある小計行も、列数・金額列の数値妥当性は
 * 通常の明細行と同じ基準で検証できるため、この段階では小計行を特別扱いしない
 * （小計行の除外・振り分けはSteraCodeFileImporter側の責務）。
 */
public class SteraCodeCsvFormatValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 10;
    private static final String BOM = "﻿";

    private static final String[] EXPECTED_HEADERS = {
        "ブランド", "端末識別番号", "伝票番号", "決済年月日", "決済時間",
        "1:売上2:返品", "決済金額", "手数料金額", "収納金額", "サブウォレット名"
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
                String salesReturnFlag = fields.get(5).trim();
                if (!isInteger(salesReturnFlag)) {
                    addNumericError(result, rowNumber, EXPECTED_HEADERS[5], salesReturnFlag);
                }
                String settlementAmount = fields.get(6).trim();
                if (!isInteger(settlementAmount)) {
                    addNumericError(result, rowNumber, EXPECTED_HEADERS[6], settlementAmount);
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
