package com.cupit.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cupit.dto.PaygateStationRow;
import com.cupit.service.PaygateStationInquiryService;

/**
 * PAYGATE Station精算情報照会画面のコントローラ。参照専用（GET）のため
 * CSRF検査の対象外（CLAUDE.mdの規約どおり更新系メソッドのみが検査対象）。
 */
@Controller
public class PaygateStationController {

    private final PaygateStationInquiryService paygateStationInquiryService;

    public PaygateStationController(PaygateStationInquiryService paygateStationInquiryService) {
        this.paygateStationInquiryService = paygateStationInquiryService;
    }

    /**
     * 全決済会社・全期間の精算明細を JSON で返す。締め日・決済会社・決済種類による
     * 絞り込みは画面側（JS）で行う。
     *
     * @return 明細一覧 JSON
     */
    @GetMapping(value = "/paygate_station_search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<PaygateStationRow>> search() {
        return ResponseEntity.ok(paygateStationInquiryService.findAll());
    }

}
