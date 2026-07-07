package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * PAYGATE 会員コード紐付 CSV のフォーマットを検証するクラス。
 * 13列固定、1行目は内容によらず常にヘッダー行として扱う（列名の一致チェックは行わない。
 * 列名は変更されることがあるため、ヘッダー行の存在確認と列数のみを検証対象とする）。
 * hana cupid管理番号（取引コード）は必須。1取引コード（加盟店）に複数端末が存在する
 * 運用があるため、取引コード自体の重複は許容する。代わりに、各決済会社の精算データを
 * 取り込む際に取引コードを逆引きするキーとなる項目（端末識別番号・住信SBI加盟店番号・
 * ネットスターズ店舗コード・楽天ペイ店舗コード）がCSV内で重複していないかを検証する。
 */
@Component
public class PaygateMappingCsvValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 13;

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
            Set<String> seenTerminalIds = new HashSet<>();
            Set<String> seenSbiMerchantIds = new HashSet<>();
            Set<String> seenNetstarStoreCodes = new HashSet<>();
            Set<String> seenRpayStoreCodes = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null && rowNumber <= getMaxRowsToValidate()) {
                rowNumber++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    result.addError(new CsvValidationError(rowNumber, "hana cupid管理番号",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                validateDataRow(result, rowNumber, fields,
                        seenTerminalIds, seenSbiMerchantIds,
                        seenNetstarStoreCodes, seenRpayStoreCodes);
            }
            result.setTotalRowCount(rowNumber - 1);
        }
        return result;
    }

    /**
     * 取引コード自体の重複は許容し（1取引コードに複数端末が存在するため）、
     * 精算データ取込み時の逆引きキーとなる4項目のCSV内重複のみを検証する。
     * リーダーシリアル番号・JCB加盟店番号は重複を許容する（後者は1店舗が複数端末で
     * 同一のJCB契約を共有するケースが実データ上正常に存在するため対象外とする）。
     */
    private void validateDataRow(
            CsvValidationResult result, int rowNumber, List<String> fields,
            Set<String> seenTerminalIds, Set<String> seenSbiMerchantIds,
            Set<String> seenNetstarStoreCodes, Set<String> seenRpayStoreCodes) {
        String tradeCode = fields.get(0).trim();
        if (tradeCode.isEmpty()) {
            result.addError(new CsvValidationError(
                    rowNumber, "hana cupid管理番号", "取引コードは必須です。"));
            return;
        }
        checkDuplicate(result, rowNumber, "端末識別番号",
                fields.get(3).trim(), seenTerminalIds);
        checkDuplicate(result, rowNumber, "加盟店番号(住信SBI)",
                fields.get(5).trim(), seenSbiMerchantIds);
        checkDuplicate(result, rowNumber, "StarPay店舗コード(ﾈｯﾄｽﾀｰｽﾞ)",
                fields.get(6).trim(), seenNetstarStoreCodes);
        checkDuplicate(result, rowNumber, "GW店舗コード(Rpay)",
                fields.get(9).trim(), seenRpayStoreCodes);
    }

    private void checkDuplicate(
            CsvValidationResult result, int rowNumber, String columnLabel,
            String value, Set<String> seenValues) {
        if (value.isEmpty()) {
            return;
        }
        if (!seenValues.add(value)) {
            result.addError(new CsvValidationError(rowNumber, columnLabel,
                    columnLabel + "「" + value + "」がCSV内で重複しています。"));
        }
    }

    private String stripCr(String line) {
        if (line != null && line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

}
