package com.cupit.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
 * JFTD統合振込CSV作成画面（プレビュー・確定・CSVダウンロード）と、
 * 帳票出力画面（確定済みバッチからの売上報告書・支払明細書ダウンロード）の
 * 両方が使用するダウンロードAPIを提供するコントローラ。「確定」操作自体は
 * CSV作成画面（/jftd_transfer）からのみ呼び出される。帳票出力画面（/jftd_report）は
 * 確定済みデータの参照・ダウンロード専用で、確定操作は行わない。
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
     * 確定済みの統合振込バッチの売上報告書(.xlsx)をダウンロードする。帳票出力画面の
     * 履歴一覧で複数バッチを選択した場合、それらをまとめて1つの帳票に集計する。
     * 指定されたバッチに明細が1件も無い場合（存在しない・削除済みのtransferBatchIdを
     * 指定した場合等）は、0円のプレースホルダー帳票を黙って返さず404を返す。
     */
    @GetMapping("/jftd_transfer/report/sales")
    public ResponseEntity<byte[]> downloadSalesReport(@RequestParam("ids") List<Integer> transferBatchIds) {
        List<ReportRow> rows = reportDataService.getReportRows(transferBatchIds);
        if (rows.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        byte[] xlsxBytes = salesReportXlsxWriter.write(rows);
        return fileResponse(xlsxBytes, XLSX_MEDIA_TYPE, "売上報告書_" + nowTimestamp() + ".xlsx");
    }

    /**
     * 確定済みの統合振込バッチの支払明細書(.xlsx)をダウンロードする。帳票出力画面の
     * 履歴一覧で複数バッチを選択した場合、それらをまとめて1つの帳票に集計する。
     * 指定されたバッチに明細が1件も無い場合は404を返す（downloadSalesReport()と同様）。
     */
    @GetMapping("/jftd_transfer/report/statement")
    public ResponseEntity<byte[]> downloadStatement(@RequestParam("ids") List<Integer> transferBatchIds) {
        List<ReportRow> rows = reportDataService.getReportRows(transferBatchIds);
        if (rows.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        byte[] xlsxBytes = supportStatementXlsxWriter.write(rows);
        return fileResponse(xlsxBytes, XLSX_MEDIA_TYPE, "支払明細書_" + nowTimestamp() + ".xlsx");
    }

    private static final DateTimeFormatter FILENAME_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String nowTimestamp() {
        return LocalDateTime.now().format(FILENAME_TIMESTAMP_FORMAT);
    }

    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private List<TransferLineItem> loadLineItems(int transferBatchId) {
        List<JftdTransferDetail> details = transferDetailRepository.findByTransferBatchId(transferBatchId);
        return details.stream()
                .map(d -> new TransferLineItem(
                        d.getTradeCode(), d.getItemCode(), d.getQuantity(), d.getAmount(),
                        d.getGrossAmount(), d.getAcquirerFeeTaxFree(),
                        d.getAcquirerFeeBase(), d.getAcquirerFeeTax()))
                .toList();
    }

    private ResponseEntity<byte[]> fileResponse(byte[] body, String mediaType, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(mediaType))
                .body(body);
    }

}
