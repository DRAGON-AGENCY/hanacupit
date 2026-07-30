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

import com.cupit.dto.SteraJcbRow;
import com.cupit.service.SteraJcbInquiryService;

/**
 * SteraJcbController のテスト。Serviceをモック化し、
 * データベースに依存せず動作を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraJcbControllerTest {

    @Mock
    private SteraJcbInquiryService steraJcbInquiryService;

    @Test
    void searchReturnsRowsFromServiceWithOkStatus() {
        SteraJcbController controller = new SteraJcbController(steraJcbInquiryService);
        SteraJcbRow row = new SteraJcbRow(
                "99-201", "２１８１－５００－９２－０００１０", "花キューピットstera_JCBテスト店A",
                "【ＪＣＢカード】", "◆クレジット", "リボ払い", LocalDate.of(2025, 11, 30),
                40000, 1100, 80, 38820);
        List<SteraJcbRow> rows = List.of(row);
        when(steraJcbInquiryService.findAll()).thenReturn(rows);

        ResponseEntity<List<SteraJcbRow>> response = controller.search();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(rows);
    }

}
