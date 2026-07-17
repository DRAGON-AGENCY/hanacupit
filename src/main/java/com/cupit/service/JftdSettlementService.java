package com.cupit.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvFormatValidatorFactory;
import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;
import com.cupit.csv.PaymentType;
import com.cupit.csv.importer.FileImporter;
import com.cupit.csv.importer.FileImporterFactory;
import com.cupit.csv.importer.ImportResult;
import com.cupit.dto.CsvValidationResponse;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;

/**
 * JFTD精算データ作成（PAYGATE Station）のビジネスロジックを担うサービス。
 * 決済種類に応じた INPUTファイルのフォーマット検証とデータ登録を行う。
 */
@Service
public class JftdSettlementService {

    private final FileImporterFactory fileImporterFactory;
    private final ImportBatchRepository importBatchRepository;

    public JftdSettlementService(
            FileImporterFactory fileImporterFactory,
            ImportBatchRepository importBatchRepository) {
        this.fileImporterFactory = fileImporterFactory;
        this.importBatchRepository = importBatchRepository;
    }

    /**
     * アップロードされたファイルのフォーマットを検証し、結果を返す。
     *
     * @param file        アップロードファイル
     * @param paymentType 決済種類の表示名
     * @return フォーマット検証結果
     * @throws IOException ファイル読み込みエラー
     */
    public CsvValidationResponse validateFileFormat(
            MultipartFile file, String paymentType) throws IOException {
        if (file == null || file.isEmpty()) {
            return buildValidationErrorResponse("ファイルが選択されていません。");
        }
        PaymentType type = PaymentType.fromDisplayName(paymentType);
        CsvValidationResult result = CsvFormatValidatorFactory.getValidator(type).validate(file);
        return toResponse(result);
    }

    /**
     * アップロードされたINPUTファイルをフォーマット検証のうえDBに登録する。
     *
     * @param file        アップロードファイル
     * @param paymentType 決済種類の表示名
     * @param cutoffDate  締め日（yyyy-MM-dd）。JFTD側5決済会社の精算ファイルの日付列だけ
     *                    では精算対象期間を一意に判定できないため、画面で明示入力させた
     *                    値をそのままバッチに記録する
     * @param memberNo    ログインユーザーID
     * @param replace     同じ決済種別で置き換え候補となる未確定のバッチが既に存在する場合に、
     *                    それを削除して置き換えることに同意しているかどうか
     * @return インポート結果
     * @throws IOException ファイル読み込みエラー
     */
    @Transactional(rollbackFor = IOException.class)
    public ImportResponse importFile(
            MultipartFile file,
            String paymentType,
            String cutoffDate,
            String memberNo,
            boolean replace) throws IOException {
        if (file == null || file.isEmpty()) {
            return new ImportResponse(false, 0, null, "ファイルが選択されていません。");
        }

        LocalDate parsedCutoffDate;
        try {
            parsedCutoffDate = LocalDate.parse(cutoffDate);
        } catch (DateTimeParseException e) {
            String message = (cutoffDate == null || cutoffDate.isBlank())
                    ? "締め日を入力してください。"
                    : "締め日の形式が不正です。";
            return new ImportResponse(false, 0, null, message);
        }

        PaymentType type = PaymentType.fromDisplayName(paymentType);

        CsvValidationResult validationResult =
                CsvFormatValidatorFactory.getValidator(type).validate(file);
        if (validationResult.isFatal()) {
            return new ImportResponse(false, 0, null,
                    "フォーマット検証エラー: " + buildFatalDetailMessage(validationResult));
        }

        FileImporter importer = fileImporterFactory.getImporter(type);
        String fileHash = sha256Hex(file.getBytes());

        Optional<ImportBatch> existingReplaceable =
                findReplaceableUnprocessedBatch(paymentType, fileHash);
        if (existingReplaceable.isPresent()) {
            ImportBatch existing = existingReplaceable.get();
            if (!replace) {
                return ImportResponse.replaceConfirmationRequired(new ImportResponse.ReplaceConfirmation(
                        existing.getBatchId(), existing.getFileName(),
                        existing.getRecordCount() != null ? existing.getRecordCount() : 0,
                        existing.getErrorCount() != null ? existing.getErrorCount() : 0,
                        splitLookupKeys(existing.getLookupKeys())));
            }
            importer.deleteBatchData(existing.getBatchId());
            importBatchRepository.delete(existing);
        }

        String key = importer.extractLookupKey(file);
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    type.getDisplayName() + "ファイルから識別キーを取得できませんでした。");
        }
        List<String> allKeys = importer.extractAllLookupKeys(file);

        ImportBatch batch = new ImportBatch();
        batch.setPaymentType(paymentType);
        batch.setFileName(file.getOriginalFilename());
        batch.setImportedAt(LocalDateTime.now());
        batch.setUpdateEmployee(memberNo);
        batch.setCreateDate(LocalDate.now());
        batch.setFileHash(fileHash);
        batch.setLookupKeys(String.join(",", allKeys));
        batch.setCutoffDate(parsedCutoffDate);
        ImportBatch savedBatch = importBatchRepository.save(batch);

        ImportResult result = importer.importFile(file, savedBatch);

        savedBatch.setRecordCount(result.getSuccessCount());
        savedBatch.setErrorCount(result.getErrors().size());
        importBatchRepository.save(savedBatch);

        return buildImportResponse(result, savedBatch.getBatchId());
    }

    /**
     * 指定した決済種別で、置き換え候補となる未確定（transfer_batch_id IS NULL）の
     * インポートバッチが存在すれば返す。通常運用で同じ決済種別のファイルを複数回・
     * 確定前にアップロードすること自体は正常系（未確定分はまとめて確定される）だが、
     * 次のいずれかに該当するバッチは「訂正しての再アップロード」または「同一ファイルの
     * 誤った再アップロード」の可能性が高いため、この場合だけユーザーに置き換えの
     * 確認を求める。
     * ・エラーを含んだまま残っている（訂正後の再アップロードを想定）
     * ・アップロードされたファイルと内容（SHA-256ハッシュ）が完全に一致する
     *   （同一ファイルの誤った再アップロードを想定。エラー有無は問わない）
     */
    private Optional<ImportBatch> findReplaceableUnprocessedBatch(String paymentType, String fileHash) {
        return importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(paymentType).stream()
                .filter(b -> (b.getErrorCount() != null && b.getErrorCount() > 0)
                        || fileHash.equals(b.getFileHash()))
                .findFirst();
    }

    private List<String> splitLookupKeys(String lookupKeys) {
        if (lookupKeys == null || lookupKeys.isBlank()) {
            return List.of();
        }
        return Arrays.asList(lookupKeys.split(","));
    }

    private String sha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256アルゴリズムが利用できません。", e);
        }
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
