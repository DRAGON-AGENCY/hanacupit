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
import com.cupit.csv.importer.MemberInfoFileImporter;
import com.cupit.csv.validator.MemberMasterCsvValidator;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;

/**
 * 加盟会員店マスターデータ登録・更新のビジネスロジックを担うサービス。
 * 加盟会員店マスター CSV の検証・m_member_info への登録・更新（取引コード単位のupsert）を行う。
 */
@Service
public class MemberMasterService {

    private static final String PAYMENT_TYPE_MEMBER_MASTER = "会員マスター";

    private final MemberMasterCsvValidator csvValidator;
    private final MemberInfoFileImporter fileImporter;
    private final ImportBatchRepository importBatchRepository;

    public MemberMasterService(
            MemberMasterCsvValidator csvValidator,
            MemberInfoFileImporter fileImporter,
            ImportBatchRepository importBatchRepository) {
        this.csvValidator = csvValidator;
        this.fileImporter = fileImporter;
        this.importBatchRepository = importBatchRepository;
    }

    /**
     * CSV ファイルを検証のうえ m_member_info を取引コード単位でupsert登録する。
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
        batch.setPaymentType(PAYMENT_TYPE_MEMBER_MASTER);
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

}
