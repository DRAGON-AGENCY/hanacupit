package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cupit.dto.PaygateStationRow;
import com.cupit.service.PaygateStationInquiryService;

/**
 * PaygateStationController のテスト。Serviceをモック化し、
 * データベースに依存せず動作を検証する。
 */
@ExtendWith(MockitoExtension.class)
class PaygateStationControllerTest {

    @Mock
    private PaygateStationInquiryService paygateStationInquiryService;

    @Test
    void searchReturnsRowsFromServiceWithOkStatus() {
        PaygateStationController controller =
                new PaygateStationController(paygateStationInquiryService);
        PaygateStationRow row = new PaygateStationRow(
                "01-001", "花のいのうえ", "JCB", "【ＪＣＢカード】",
                LocalDate.of(2025, 11, 30), 5, 14550, 400, 14150);
        List<PaygateStationRow> rows = List.of(row);
        when(paygateStationInquiryService.findAll()).thenReturn(rows);

        ResponseEntity<List<PaygateStationRow>> response = controller.search();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(rows);
    }

}
