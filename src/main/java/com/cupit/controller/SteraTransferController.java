package com.cupit.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cupit.dto.SteraTransferPreviewResponse;
import com.cupit.dto.TransferConfirmResponse;
import com.cupit.dto.TransferTargetFile;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.ImportBatch;
import com.cupit.model.SteraTransferDetail;
import com.cupit.repository.SteraTransferDetailRepository;
import com.cupit.service.SteraTransferCalculationService;
import com.cupit.service.SteraTransferConfirmService;
import com.cupit.service.settlement.SteraTransferCsvWriter;
import com.cupit.service.settlement.SteraTransferLineItem;

import jakarta.servlet.http.HttpSession;

/**
 * その他統合振込CSV作成画面（stera terminal、プレビュー・確定・CSVダウンロード）の
 * APIを提供するコントローラ。{@link JftdTransferController}と対になるが、
 * 項目コード方式ではなく全銀フォーマット単一CSVのため専用コントローラとする。
 */
@Controller
public class SteraTransferController {

    private final SteraTransferCalculationService calculationService;
    private final SteraTransferConfirmService confirmService;
    private final SteraTransferDetailRepository transferDetailRepository;
    private final SteraTransferCsvWriter transferCsvWriter;

    public SteraTransferController(
            SteraTransferCalculationService calculationService,
            SteraTransferConfirmService confirmService,
            SteraTransferDetailRepository transferDetailRepository,
            SteraTransferCsvWriter transferCsvWriter) {
        this.calculationService = calculationService;
        this.confirmService = confirmService;
        this.transferDetailRepository = transferDetailRepository;
        this.transferCsvWriter = transferCsvWriter;
    }

    /**
     * 未処理データを集計し、取引コード単位の明細をJSONで返す（DB書き込みなし）。
     */
    @PostMapping(value = "/stera_transfer/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<SteraTransferPreviewResponse> preview() {
        List<SteraTransferLineItem> lineItems = calculationService.calculateAllLineItems();
        List<TransferTargetFile> targetFiles = calculationService.findTargetImportBatches().stream()
                .map(this::toTargetFile)
                .toList();
        return ResponseEntity.ok(new SteraTransferPreviewResponse(lineItems, targetFiles));
    }

    private TransferTargetFile toTargetFile(ImportBatch batch) {
        return new TransferTargetFile(
                batch.getBatchId(), batch.getPaymentType(), batch.getFileName(), batch.getCutoffDate(),
                batch.getRecordCount() != null ? batch.getRecordCount() : 0);
    }

    /**
     * 未処理データを確定し、その他統合振込バッチを作成する。
     */
    @PostMapping(value = "/stera_transfer/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<TransferConfirmResponse> confirm(HttpSession session) {
        try {
            Object loginUserAttr = session.getAttribute(
                    AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER);
            String loginUser = (loginUserAttr != null) ? loginUserAttr.toString() : "UNKNOWN";
            int transferBatchId = confirmService.confirm(loginUser);
            return ResponseEntity.ok(new TransferConfirmResponse(true, transferBatchId, null));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(new TransferConfirmResponse(false, null, e.getMessage()));
        }
    }

    /**
     * 確定済みのその他統合振込バッチのCSV（全銀フォーマット単一ファイル）をダウンロードする。
     */
    @GetMapping("/stera_transfer/{transferBatchId}/csv")
    public ResponseEntity<byte[]> downloadCsv(@PathVariable int transferBatchId) {
        List<SteraTransferDetail> details = transferDetailRepository.findByTransferBatchId(transferBatchId);
        List<SteraTransferLineItem> lineItems = details.stream()
                .map(d -> new SteraTransferLineItem(
                        d.getTradeCode(), d.getGrossAmount(), d.getAcquirerFee(), d.getCompanyFee(),
                        d.getTransferFee(), d.getNetAmount(), d.getBankCode(), d.getBankName(),
                        d.getBankBranchCode(), d.getBranchName(), d.getAccountType(),
                        d.getAccountNo(), d.getAccountHolderKana()))
                .toList();
        byte[] csvBytes = transferCsvWriter.writeCsv(lineItems);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("stera_transfer_" + transferBatchId + ".csv", StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

}
