package com.cupit.csv.validator;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationResult;

/**
 * 決済種類ごとのINPUTファイルフォーマット検証を行うインタフェース。
 */
public interface CsvFormatValidator {

    CsvValidationResult validate(MultipartFile file) throws IOException;
}
