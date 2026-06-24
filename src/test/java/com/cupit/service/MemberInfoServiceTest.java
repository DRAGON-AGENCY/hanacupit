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

    private static final String DEFAULT_TRANSACTION_CODE = "01-001";

    @Mock
    private MemberInfoRepository memberInfoRepository;

    @InjectMocks
    private MemberInfoService memberInfoService;

    @Test
    void findByTransactionCodeReturnsMemberInfoWhenFound() {
        MemberInfo expected = new MemberInfo();
        expected.setTransactionCode(DEFAULT_TRANSACTION_CODE);
        expected.setStoreName("赤坂生花店");
        when(memberInfoRepository.findById(DEFAULT_TRANSACTION_CODE))
                .thenReturn(Optional.of(expected));

        MemberInfo actual =
                memberInfoService.findByTransactionCode(DEFAULT_TRANSACTION_CODE);

        assertThat(actual.getTransactionCode())
                .isEqualTo(DEFAULT_TRANSACTION_CODE);
        assertThat(actual.getStoreName()).isEqualTo("赤坂生花店");
    }

    @Test
    void findByTransactionCodeUsesDefaultCodeWhenBlank() {
        MemberInfo expected = new MemberInfo();
        expected.setTransactionCode(DEFAULT_TRANSACTION_CODE);
        when(memberInfoRepository.findById(DEFAULT_TRANSACTION_CODE))
                .thenReturn(Optional.of(expected));

        MemberInfo actual = memberInfoService.findByTransactionCode("");

        assertThat(actual.getTransactionCode())
                .isEqualTo(DEFAULT_TRANSACTION_CODE);
    }

    @Test
    void findByTransactionCodeThrowsExceptionWhenNotFound() {
        when(memberInfoRepository.findById("99-999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> memberInfoService.findByTransactionCode("99-999"))
                .isInstanceOf(MemberInfoNotFoundException.class);
    }
}
