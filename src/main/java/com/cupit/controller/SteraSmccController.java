package com.cupit.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cupit.dto.SteraSmccRow;
import com.cupit.service.SteraSmccInquiryService;

/**
 * stera terminal精算情報照会(SMCC)画面のコントローラ。参照専用（GET）のため
 * CSRF検査の対象外（CLAUDE.mdの規約どおり更新系メソッドのみが検査対象）。
 */
@Controller
public class SteraSmccController {

    private final SteraSmccInquiryService steraSmccInquiryService;

    public SteraSmccController(SteraSmccInquiryService steraSmccInquiryService) {
        this.steraSmccInquiryService = steraSmccInquiryService;
    }

    /**
     * 全期間のsteraクレジット・stera code精算明細を JSON で返す。締め日・決済フォーマット・
     * カード名・取扱区分による絞り込みは画面側（JS）で行う。
     *
     * @return 明細一覧 JSON
     */
    @GetMapping(value = "/stera_smcc_search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<SteraSmccRow>> search() {
        return ResponseEntity.ok(steraSmccInquiryService.findAll());
    }

}
