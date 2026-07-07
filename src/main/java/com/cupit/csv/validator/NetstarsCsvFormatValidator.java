package com.cupit.csv.validator;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * ネットスターズ 店舗コード別集計ファイルのフォーマットを検証するクラス。
 * ファイル形式：Excel（.xlsx）。Apache POI 非搭載のため拡張子のみ検査する。
 */
public class NetstarsCsvFormatValidator extends AbstractCsvFormatValidator {

    @Override
    public CsvValidationResult validate(MultipartFile file) throws IOException {
        CsvValidationResult result = new CsvValidationResult();

        String ext = getExtension(file.getOriginalFilename());
        if (!"xlsx".equals(ext)) {
            result.addError(new CsvValidationError(
                    0, "", "ファイルの拡張子が不正です。期待: .xlsx、実際: ." + ext));
            result.markFatal();
        }

        return result;
    }
}
