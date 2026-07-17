package com.cupit.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.writer.MerchantNumberDataCsvWriter;
import com.cupit.csv.writer.ShopDataCsvWriter;
import com.cupit.csv.writer.TerminalDataCsvWriter;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.repository.MerchantNumberDataRepository;
import com.cupit.repository.ShopDataRepository;
import com.cupit.repository.TerminalDataRepository;
import com.cupit.service.ShopDataService;
import com.cupit.service.ShopDataService.DataType;

import jakarta.servlet.http.HttpSession;

/**
 * 「店舗・端末・加盟店番号データ作成」画面のコントローラ。
 * データ種類（店舗データ／端末データ／加盟店番号データ）ごとにCSVアップロード・
 * ダウンロードをルーティングする。
 */
@Controller
public class ShopDataController {

    private static final DateTimeFormatter FILENAME_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ShopDataService shopDataService;
    private final ShopDataRepository shopDataRepository;
    private final TerminalDataRepository terminalDataRepository;
    private final MerchantNumberDataRepository merchantNumberDataRepository;
    private final ShopDataCsvWriter shopDataCsvWriter;
    private final TerminalDataCsvWriter terminalDataCsvWriter;
    private final MerchantNumberDataCsvWriter merchantNumberDataCsvWriter;

    public ShopDataController(
            ShopDataService shopDataService,
            ShopDataRepository shopDataRepository,
            TerminalDataRepository terminalDataRepository,
            MerchantNumberDataRepository merchantNumberDataRepository,
            ShopDataCsvWriter shopDataCsvWriter,
            TerminalDataCsvWriter terminalDataCsvWriter,
            MerchantNumberDataCsvWriter merchantNumberDataCsvWriter) {
        this.shopDataService = shopDataService;
        this.shopDataRepository = shopDataRepository;
        this.terminalDataRepository = terminalDataRepository;
        this.merchantNumberDataRepository = merchantNumberDataRepository;
        this.shopDataCsvWriter = shopDataCsvWriter;
        this.terminalDataCsvWriter = terminalDataCsvWriter;
        this.merchantNumberDataCsvWriter = merchantNumberDataCsvWriter;
    }

    /**
     * CSV をデータ種類に応じて検証のうえ登録し、結果を JSON で返す。
     *
     * @param dataType データ種類（SHOP／TERMINAL／MERCHANT_NUMBER）
     * @param file     アップロードファイル
     * @param session  HTTPセッション（ログインユーザー取得用）
     * @return インポート結果 JSON
     */
    @PostMapping(value = "/shop_data/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ImportResponse> upload(
            @RequestParam("dataType") DataType dataType,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            Object loginUserAttr = session.getAttribute(
                    AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER);
            String loginUser = (loginUserAttr != null) ? loginUserAttr.toString() : "UNKNOWN";
            ImportResponse response = shopDataService.importFile(dataType, file, loginUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ImportResponse(false, 0, null, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ImportResponse(
                            false, 0, null,
                            "ファイルの読み込みに失敗しました: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(new ImportResponse(
                            false, 0, null,
                            "予期せぬエラーが発生しました: " + e.getMessage()));
        }
    }

    /**
     * データ種類に応じた現在の登録データをCSVダウンロードする。
     * ダウンロードしたファイルはそのまま編集して再アップロードできる。
     *
     * @param dataType データ種類（SHOP／TERMINAL／MERCHANT_NUMBER）
     * @return CSVファイル（UTF-8 BOM付き）
     */
    @GetMapping("/shop_data/download")
    public ResponseEntity<byte[]> download(@RequestParam("dataType") DataType dataType) {
        byte[] csvBytes = switch (dataType) {
            case SHOP -> shopDataCsvWriter.writeCsv(shopDataRepository.findAllByOrderByTradeCodeAsc());
            case TERMINAL ->
                terminalDataCsvWriter.writeCsv(terminalDataRepository.findAllByOrderByTradeCodeAsc());
            case MERCHANT_NUMBER -> merchantNumberDataCsvWriter.writeCsv(
                    merchantNumberDataRepository.findAllByOrderByTradeCodeAsc());
        };

        String prefix = switch (dataType) {
            case SHOP -> "shop_data_";
            case TERMINAL -> "terminal_data_";
            case MERCHANT_NUMBER -> "merchant_number_data_";
        };
        String filename = prefix + LocalDate.now().format(FILENAME_DATE_FORMAT) + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

}
