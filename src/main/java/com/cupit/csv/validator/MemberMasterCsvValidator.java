package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * 加盟会員店マスターデータ CSV のフォーマットを検証するクラス。
 * 255列固定、1行目は内容によらず常にヘッダー行として扱う（列名の一致チェックは行わない）。
 * 取引コードは必須。trade_codeは m_member_info の主キーであり1取引コード=1行のため、
 * CSV内での取引コードの重複はエラーとする（PAYGATEマッピングとは異なり重複を許容しない）。
 */
@Component
public class MemberMasterCsvValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 255;

    private final PaymentCompanyFormatChecker paymentCompanyFormatChecker;

    public MemberMasterCsvValidator(PaymentCompanyFormatChecker paymentCompanyFormatChecker) {
        this.paymentCompanyFormatChecker = paymentCompanyFormatChecker;
    }

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
            Set<String> seenTradeCodes = new HashSet<>();
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
                validateDataRow(result, rowNumber, fields, seenTradeCodes);
            }
            result.setTotalRowCount(rowNumber - 1);
        }
        return result;
    }

    private void validateDataRow(
            CsvValidationResult result, int rowNumber, List<String> fields,
            Set<String> seenTradeCodes) {
        String tradeCode = fields.get(0).trim();
        if (tradeCode.isEmpty()) {
            result.addError(new CsvValidationError(rowNumber, "取引コード", "取引コードは必須です。"));
            return;
        }
        if (!seenTradeCodes.add(tradeCode)) {
            result.addError(new CsvValidationError(rowNumber, "取引コード",
                    "取引コード「" + tradeCode + "」がCSV内で重複しています。"));
            return;
        }
        List<CsvValidationError> paymentCompanyErrors = new ArrayList<>();
        paymentCompanyFormatChecker.check(fields, rowNumber, paymentCompanyErrors);
        paymentCompanyErrors.forEach(result::addError);
    }

    private String stripCr(String line) {
        if (line != null && line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

}
