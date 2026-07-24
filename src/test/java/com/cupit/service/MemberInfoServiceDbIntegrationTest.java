package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * {@link MemberInfoService#findByTradeCode(String)} が実際に m_member_info テーブルへ
 * 登録した値を、モック化しない実リポジトリ・実DB接続で読み出せることを検証する。
 * {@link MemberInfoServiceTest}はRepositoryをモック化しており「保存したJavaオブジェクトの
 * 値が正しいか」までしか保証しないため、実際のINSERT・カラムマッピングが正しく
 * 行われることをここで別途確認する。{@code @DataJpaTest}は各テストメソッドを
 * トランザクションで囲み終了後に自動ロールバックするため、開発DBへ永続的な
 * データは残らない。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberInfoServiceDbIntegrationTest {

    private static final String TEST_TRADE_CODE = "99-994";

    @Autowired
    private MemberInfoRepository memberInfoRepository;

    @Test
    void findByTradeCodeReadsBackPersistedRow() {
        MemberInfo record = new MemberInfo();
        record.setTradeCode(TEST_TRADE_CODE);
        record.setCreateDate(LocalDate.now());
        record.setStoreName("テスト花店");
        record.setStoreNameKana("テストハナテン");
        record.setQualificationType("正会員");
        memberInfoRepository.saveAndFlush(record);

        MemberInfoService memberInfoService = new MemberInfoService(memberInfoRepository);
        MemberInfo reloaded = memberInfoService.findByTradeCode(TEST_TRADE_CODE);

        assertThat(reloaded.getTradeCode()).isEqualTo(TEST_TRADE_CODE);
        assertThat(reloaded.getStoreName()).isEqualTo("テスト花店");
        assertThat(reloaded.getStoreNameKana()).isEqualTo("テストハナテン");
        assertThat(reloaded.getQualificationType()).isEqualTo("正会員");
    }

}
