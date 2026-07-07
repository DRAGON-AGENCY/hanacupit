package com.cupit.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;
import com.cupit.csv.importer.ImportResult;
import com.cupit.csv.importer.PaygateMappingFileImporter;
import com.cupit.csv.validator.PaygateMappingCsvValidator;
import com.cupit.dto.CsvValidationResponse;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;

/**
 * 取引コード紐付データ作成のビジネスロジックを担うサービス。
 * PAYGATE 会員コード紐付 CSV の検証・取引コード単位での洗い替え登録を行う。
 */
@Service
public class PaygateMappingService {

    private static final String PAYMENT_TYPE_PAYGATE_MAPPING = "PAYGATEマッピング";

    private final PaygateMappingCsvValidator csvValidator;
    private final PaygateMappingFileImporter fileImporter;
    private final ImportBatchRepository importBatchRepository;

    public PaygateMappingService(
            PaygateMappingCsvValidator csvValidator,
            PaygateMappingFileImporter fileImporter,
            ImportBatchRepository importBatchRepository) {
        this.csvValidator = csvValidator;
        this.fileImporter = fileImporter;
        this.importBatchRepository = importBatchRepository;
    }

    /**
     * CSV ファイルのフォーマットを検証し、結果を返す。
     *
     * @param file アップロードファイル
     * @return フォーマット検証結果
     * @throws IOException ファイル読み込みエラー
     */
    public CsvValidationResponse validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return buildValidationErrorResponse("ファイルが選択されていません。");
        }
        CsvValidationResult result = csvValidator.validate(file);
        return toResponse(result);
    }

    /**
     * CSV ファイルを検証のうえ m_paygate_store_mapping を取引コード単位で洗い替える。
     * CSVに存在しない取引コードの既存レコードは削除されない。
     *
     * @param file      アップロードファイル
     * @param loginUser ログインユーザーID
     * @return インポート結果
     * @throws IOException ファイル読み込みエラー
     */
    @Transactional(rollbackFor = IOException.class)
    public ImportResponse importFile(
            MultipartFile file, String loginUser) throws IOException {
        if (file == null || file.isEmpty()) {
            return new ImportResponse(false, 0, null, "ファイルが選択されていません。");
        }

        CsvValidationResult validationResult = csvValidator.validate(file);
        if (validationResult.isFatal()) {
            return new ImportResponse(false, 0, null,
                    "フォーマット検証エラー: " + buildFatalDetailMessage(validationResult));
        }

        ImportBatch batch = new ImportBatch();
        batch.setPaymentType(PAYMENT_TYPE_PAYGATE_MAPPING);
        batch.setFileName(file.getOriginalFilename());
        batch.setImportedAt(LocalDateTime.now());
        batch.setUpdateEmployee(loginUser);
        batch.setCreateDate(LocalDate.now());
        ImportBatch savedBatch = importBatchRepository.save(batch);

        ImportResult result = fileImporter.importFile(file, savedBatch);

        savedBatch.setRecordCount(result.getSuccessCount());
        savedBatch.setErrorCount(result.getErrors().size());
        importBatchRepository.save(savedBatch);

        return buildImportResponse(result, savedBatch.getBatchId());
    }

    private String buildFatalDetailMessage(CsvValidationResult validationResult) {
        if (validationResult.getErrors().isEmpty()) {
            return "フォーマットエラー";
        }
        CsvValidationError error = validationResult.getErrors().get(0);
        return error.getRowNumber() > 0
                ? error.getRowNumber() + "行目: " + error.getMessage()
                : error.getMessage();
    }

    private ImportResponse buildImportResponse(ImportResult result, Integer batchId) {
        if (!result.hasErrors()) {
            return new ImportResponse(true, result.getSuccessCount(), batchId, null);
        }
        List<ImportResponse.ErrorDetail> details = result.getErrors().stream()
                .map(e -> new ImportResponse.ErrorDetail(
                        e.getRowNumber(), e.getColumnName(), e.getMessage()))
                .collect(Collectors.toList());
        String message = "登録件数: " + result.getSuccessCount() + " 件、エラー: "
                + result.getErrors().size() + " 件（データ行数: " + result.getTotalRowCount() + "行）。"
                + "エラーが発生した行は登録されていません。";
        return new ImportResponse(
                false, result.getSuccessCount(), batchId, message,
                details, result.getTotalRowCount(), false);
    }

    private CsvValidationResponse toResponse(CsvValidationResult result) {
        List<CsvValidationResponse.ErrorDetail> details = result.getErrors().stream()
                .map(e -> new CsvValidationResponse.ErrorDetail(
                        e.getRowNumber(), e.getColumnName(), e.getMessage()))
                .collect(Collectors.toList());
        return new CsvValidationResponse(
                result.isValid(),
                result.getTotalRowCount(),
                result.isErrorLimitReached(),
                details);
    }

    private CsvValidationResponse buildValidationErrorResponse(String message) {
        return new CsvValidationResponse(
                false, 0, false,
                List.of(new CsvValidationResponse.ErrorDetail(0, "", message)));
    }

}
