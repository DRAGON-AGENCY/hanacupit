package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.exception.MemberInfoNotFoundException;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * MemberInfoService のテスト。
 * リポジトリをモック化し、データベースに依存せず動作を検証する。
 */
@ExtendWith(MockitoExtension.class)
class MemberInfoServiceTest {

    private static final String DEFAULT_TRADE_CODE = "01-001";

    @Mock
    private MemberInfoRepository memberInfoRepository;

    @InjectMocks
    private MemberInfoService memberInfoService;

    @Test
    void findByTradeCodeReturnsMemberInfoWhenFound() {
        MemberInfo expected = new MemberInfo();
        expected.setTradeCode(DEFAULT_TRADE_CODE);
        expected.setStoreName("赤坂生花店");
        when(memberInfoRepository.findById(DEFAULT_TRADE_CODE))
                .thenReturn(Optional.of(expected));

        MemberInfo actual =
                memberInfoService.findByTradeCode(DEFAULT_TRADE_CODE);

        assertThat(actual.getTradeCode())
                .isEqualTo(DEFAULT_TRADE_CODE);
        assertThat(actual.getStoreName()).isEqualTo("赤坂生花店");
    }

    @Test
    void findByTradeCodeThrowsExceptionWhenBlank() {
        when(memberInfoRepository.findById(""))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberInfoService.findByTradeCode(""))
                .isInstanceOf(MemberInfoNotFoundException.class);
    }

    @Test
    void findByTradeCodeThrowsExceptionWhenNull() {
        when(memberInfoRepository.findById(""))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberInfoService.findByTradeCode(null))
                .isInstanceOf(MemberInfoNotFoundException.class);
    }

    @Test
    void findByTradeCodeThrowsExceptionWhenNotFound() {
        when(memberInfoRepository.findById("99-999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> memberInfoService.findByTradeCode("99-999"))
                .isInstanceOf(MemberInfoNotFoundException.class);
    }
}
