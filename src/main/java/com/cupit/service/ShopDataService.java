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
import com.cupit.csv.importer.SmccMerchantNoFileImporter;
import com.cupit.csv.importer.SteraStoreFileImporter;
import com.cupit.csv.importer.SteraTerminalFileImporter;
import com.cupit.csv.validator.SmccMerchantNoCsvValidator;
import com.cupit.csv.validator.SteraStoreCsvValidator;
import com.cupit.csv.validator.SteraTerminalCsvValidator;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;

/**
 * 「店舗・端末・加盟店番号データ作成」画面のビジネスロジックを担うサービス。
 * データ種類（店舗データ／端末データ／加盟店番号データ）ごとに異なる検証・登録処理
 * （店舗データは取引コード単位のupsert、端末データ・加盟店番号データは取引コード単位の
 * 洗い替え）へルーティングする。
 */
@Service
public class ShopDataService {

    /**
     * 画面の「データ種類」セレクトに対応するデータ種類。
     */
    public enum DataType {
        SHOP("店舗データ"),
        TERMINAL("端末データ"),
        MERCHANT_NUMBER("加盟店番号データ");

        private final String label;

        DataType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final SteraStoreCsvValidator steraStoreCsvValidator;
    private final SteraStoreFileImporter steraStoreFileImporter;
    private final SteraTerminalCsvValidator steraTerminalCsvValidator;
    private final SteraTerminalFileImporter steraTerminalFileImporter;
    private final SmccMerchantNoCsvValidator smccMerchantNoCsvValidator;
    private final SmccMerchantNoFileImporter smccMerchantNoFileImporter;
    private final ImportBatchRepository importBatchRepository;

    public ShopDataService(
            SteraStoreCsvValidator steraStoreCsvValidator,
            SteraStoreFileImporter steraStoreFileImporter,
            SteraTerminalCsvValidator steraTerminalCsvValidator,
            SteraTerminalFileImporter steraTerminalFileImporter,
            SmccMerchantNoCsvValidator smccMerchantNoCsvValidator,
            SmccMerchantNoFileImporter smccMerchantNoFileImporter,
            ImportBatchRepository importBatchRepository) {
        this.steraStoreCsvValidator = steraStoreCsvValidator;
        this.steraStoreFileImporter = steraStoreFileImporter;
        this.steraTerminalCsvValidator = steraTerminalCsvValidator;
        this.steraTerminalFileImporter = steraTerminalFileImporter;
        this.smccMerchantNoCsvValidator = smccMerchantNoCsvValidator;
        this.smccMerchantNoFileImporter = smccMerchantNoFileImporter;
        this.importBatchRepository = importBatchRepository;
    }

    /**
     * CSV ファイルをデータ種類に応じて検証のうえ登録する。
     *
     * @param dataType  データ種類（店舗データ／端末データ／加盟店番号データ）
     * @param file      アップロードファイル
     * @param loginUser ログインユーザーID
     * @return インポート結果
     * @throws IOException ファイル読み込みエラー
     */
    @Transactional(rollbackFor = IOException.class)
    public ImportResponse importFile(
            DataType dataType, MultipartFile file, String loginUser) throws IOException {
        if (file == null || file.isEmpty()) {
            return new ImportResponse(false, 0, null, "ファイルが選択されていません。");
        }

        CsvValidationResult validationResult = validate(dataType, file);
        if (validationResult.isFatal()) {
            return new ImportResponse(false, 0, null,
                    "フォーマット検証エラー: " + buildFatalDetailMessage(validationResult));
        }

        ImportBatch batch = new ImportBatch();
        batch.setPaymentType(dataType.getLabel());
        batch.setFileName(file.getOriginalFilename());
        batch.setImportedAt(LocalDateTime.now());
        batch.setUpdateEmployee(loginUser);
        batch.setCreateDate(LocalDate.now());
        ImportBatch savedBatch = importBatchRepository.save(batch);

        ImportResult result = doImport(dataType, file, savedBatch);

        savedBatch.setRecordCount(result.getSuccessCount());
        savedBatch.setErrorCount(result.getErrors().size());
        importBatchRepository.save(savedBatch);

        return buildImportResponse(result, savedBatch.getBatchId());
    }

    private CsvValidationResult validate(DataType dataType, MultipartFile file) throws IOException {
        return switch (dataType) {
            case SHOP -> steraStoreCsvValidator.validate(file);
            case TERMINAL -> steraTerminalCsvValidator.validate(file);
            case MERCHANT_NUMBER -> smccMerchantNoCsvValidator.validate(file);
        };
    }

    private ImportResult doImport(
            DataType dataType, MultipartFile file, ImportBatch batch) throws IOException {
        return switch (dataType) {
            case SHOP -> steraStoreFileImporter.importFile(file, batch);
            case TERMINAL -> steraTerminalFileImporter.importFile(file, batch);
            case MERCHANT_NUMBER -> smccMerchantNoFileImporter.importFile(file, batch);
        };
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
