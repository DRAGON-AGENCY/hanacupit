package com.cupit.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

import com.cupit.csv.writer.MemberMasterCsvWriter;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;
import com.cupit.service.MemberMasterService;

import jakarta.servlet.http.HttpSession;

/**
 * 加盟会員店マスターデータ登録・更新のコントローラ。
 * CSV アップロード（フォーマット検証・取引コード単位でのupsert登録）と、
 * 現在の登録データのCSVダウンロードを処理する。
 */
@Controller
public class MemberMasterController {

    private static final DateTimeFormatter FILENAME_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MemberMasterService memberMasterService;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberMasterCsvWriter memberMasterCsvWriter;

    public MemberMasterController(
            MemberMasterService memberMasterService,
            MemberInfoRepository memberInfoRepository,
            MemberMasterCsvWriter memberMasterCsvWriter) {
        this.memberMasterService = memberMasterService;
        this.memberInfoRepository = memberInfoRepository;
        this.memberMasterCsvWriter = memberMasterCsvWriter;
    }

    /**
     * CSV を検証のうえ m_member_info を取引コード単位でupsertし、結果を JSON で返す。
     *
     * @param file    アップロードファイル
     * @param session HTTPセッション（ログインユーザー取得用）
     * @return インポート結果 JSON
     */
    @PostMapping(value = "/member_master/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ImportResponse> upload(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            Object loginUserAttr = session.getAttribute(
                    AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER);
            String loginUser = (loginUserAttr != null) ? loginUserAttr.toString() : "UNKNOWN";
            ImportResponse response = memberMasterService.importFile(file, loginUser);
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
     * m_member_info の全件を「加盟会員店マスターデータ登録・更新」CSVフォーマット
     * 仕様書と同じ255列でCSVダウンロードする。ダウンロードしたファイルはそのまま
     * 編集して再アップロードできる。
     *
     * @return CSVファイル（UTF-8 BOM付き）
     */
    @GetMapping("/member_master/download")
    public ResponseEntity<byte[]> download() {
        List<MemberInfo> records = memberInfoRepository.findAllByOrderByTradeCodeAsc();
        byte[] csvBytes = memberMasterCsvWriter.writeCsv(records);

        String filename = "member_master_" + LocalDate.now().format(FILENAME_DATE_FORMAT) + ".csv";
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
