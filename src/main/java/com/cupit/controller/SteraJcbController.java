package com.cupit.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cupit.dto.SteraJcbRow;
import com.cupit.service.SteraJcbInquiryService;

/**
 * stera terminal精算情報照会(JCB)画面のコントローラ。参照専用（GET）のため
 * CSRF検査の対象外（CLAUDE.mdの規約どおり更新系メソッドのみが検査対象）。
 */
@Controller
public class SteraJcbController {

    private final SteraJcbInquiryService steraJcbInquiryService;

    public SteraJcbController(SteraJcbInquiryService steraJcbInquiryService) {
        this.steraJcbInquiryService = steraJcbInquiryService;
    }

    /**
     * 全期間のstera JCB精算明細を JSON で返す。締め日・カード名・支払方法による
     * 絞り込みは画面側（JS）で行う。
     *
     * @return 明細一覧 JSON
     */
    @GetMapping(value = "/stera_jcb_search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<SteraJcbRow>> search() {
        return ResponseEntity.ok(steraJcbInquiryService.findAll());
    }

}
