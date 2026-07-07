package com.cupit.controller;

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

import com.cupit.dto.TransferConfirmResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.JftdTransferDetail;
import com.cupit.repository.JftdTransferDetailRepository;
import com.cupit.service.JftdReportDataService;
import com.cupit.service.JftdTransferCalculationService;
import com.cupit.service.JftdTransferConfirmService;
import com.cupit.service.settlement.ReportRow;
import com.cupit.service.settlement.SalesReportXlsxWriter;
import com.cupit.service.settlement.SupportStatementXlsxWriter;
import com.cupit.service.settlement.TransferCsvWriter;
import com.cupit.service.settlement.TransferLineItem;

import jakarta.servlet.http.HttpSession;

/**
 * JFTD統合振込CSV作成・帳票出力画面のプレビュー・確定・ダウンロードを処理するコントローラ。
 */
@Controller
public class JftdTransferController {

    private final JftdTransferCalculationService calculationService;
    private final JftdTransferConfirmService confirmService;
    private final JftdReportDataService reportDataService;
    private final JftdTransferDetailRepository transferDetailRepository;
    private final TransferCsvWriter transferCsvWriter;
    private final SalesReportXlsxWriter salesReportXlsxWriter;
    private final SupportStatementXlsxWriter supportStatementXlsxWriter;

    public JftdTransferController(
            JftdTransferCalculationService calculationService,
            JftdTransferConfirmService confirmService,
            JftdReportDataService reportDataService,
            JftdTransferDetailRepository transferDetailRepository,
            TransferCsvWriter transferCsvWriter,
            SalesReportXlsxWriter salesReportXlsxWriter,
            SupportStatementXlsxWriter supportStatementXlsxWriter) {
        this.calculationService = calculationService;
        this.confirmService = confirmService;
        this.reportDataService = reportDataService;
        this.transferDetailRepository = transferDetailRepository;
        this.transferCsvWriter = transferCsvWriter;
        this.salesReportXlsxWriter = salesReportXlsxWriter;
        this.supportStatementXlsxWriter = supportStatementXlsxWriter;
    }

    /**
     * 未処理データを集計し、決済会社×カードブランド単位のサマリをJSONで返す
     * （DB書き込みなし）。
     */
    @PostMapping(value = "/jftd_transfer/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ReportRow>> preview() {
        List<TransferLineItem> lineItems = calculationService.calculateAllLineItems();
        return ResponseEntity.ok(reportDataService.summarize(lineItems));
    }

    /**
     * 未処理データを確定し、統合振込バッチを作成する。
     */
    @PostMapping(value = "/jftd_transfer/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
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
     * 確定済みの統合振込バッチのCSV（項目コードごとに1ファイル）をZIPでダウンロードする。
     */
    @GetMapping("/jftd_transfer/{transferBatchId}/csv")
    public ResponseEntity<byte[]> downloadCsv(@PathVariable int transferBatchId) {
        List<TransferLineItem> lineItems = loadLineItems(transferBatchId);
        byte[] zipBytes = transferCsvWriter.writeZip(lineItems);
        return fileResponse(zipBytes, "application/zip", "jftd_transfer_" + transferBatchId + ".zip");
    }

    /**
     * 確定済みの統合振込バッチの売上報告書(.xlsx)をダウンロードする。
     */
    @GetMapping("/jftd_transfer/{transferBatchId}/report/sales")
    public ResponseEntity<byte[]> downloadSalesReport(@PathVariable int transferBatchId) {
        List<ReportRow> rows = reportDataService.getReportRows(transferBatchId);
        byte[] xlsxBytes = salesReportXlsxWriter.write(rows);
        return fileResponse(xlsxBytes, XLSX_MEDIA_TYPE, "sales_report_" + transferBatchId + ".xlsx");
    }

    /**
     * 確定済みの統合振込バッチの支払明細書(.xlsx)をダウンロードする。
     */
    @GetMapping("/jftd_transfer/{transferBatchId}/report/statement")
    public ResponseEntity<byte[]> downloadStatement(@PathVariable int transferBatchId) {
        List<ReportRow> rows = reportDataService.getReportRows(transferBatchId);
        byte[] xlsxBytes = supportStatementXlsxWriter.write(rows);
        return fileResponse(xlsxBytes, XLSX_MEDIA_TYPE, "support_statement_" + transferBatchId + ".xlsx");
    }

    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private List<TransferLineItem> loadLineItems(int transferBatchId) {
        List<JftdTransferDetail> details = transferDetailRepository.findByTransferBatchId(transferBatchId);
        return details.stream()
                .map(d -> new TransferLineItem(
                        d.getTradeCode(), d.getItemCode(), d.getQuantity(), d.getAmount()))
                .toList();
    }

    private ResponseEntity<byte[]> fileResponse(byte[] body, String mediaType, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(mediaType))
                .body(body);
    }

}
