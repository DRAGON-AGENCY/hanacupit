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

import com.cupit.dto.SteraSmccRow;
import com.cupit.service.SteraSmccInquiryService;

/**
 * SteraSmccController のテスト。Serviceをモック化し、
 * データベースに依存せず動作を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraSmccControllerTest {

    @Mock
    private SteraSmccInquiryService steraSmccInquiryService;

    @Test
    void searchReturnsRowsFromServiceWithOkStatus() {
        SteraSmccController controller = new SteraSmccController(steraSmccInquiryService);
        SteraSmccRow row = new SteraSmccRow(
                "99-301", "79890301", "花キューピットsteraクレジットテスト店A",
                "steraクレジット", "ＶＭ", "１回払", LocalDate.of(2025, 11, 30),
                20000, 550, 40, 19410);
        List<SteraSmccRow> rows = List.of(row);
        when(steraSmccInquiryService.findAll()).thenReturn(rows);

        ResponseEntity<List<SteraSmccRow>> response = controller.search();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(rows);
    }

}
