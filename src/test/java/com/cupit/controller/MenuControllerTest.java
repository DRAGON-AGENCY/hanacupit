package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.cupit.exception.MemberInfoNotFoundException;
import com.cupit.model.JftdTransferBatch;
import com.cupit.model.MemberInfo;
import com.cupit.model.SmccMerchantNo;
import com.cupit.model.SteraStore;
import com.cupit.model.SteraTerminal;
import com.cupit.model.SteraTransferBatch;
import com.cupit.repository.JftdTransferBatchRepository;
import com.cupit.repository.SmccMerchantNoRepository;
import com.cupit.repository.SteraStoreRepository;
import com.cupit.repository.SteraTerminalRepository;
import com.cupit.repository.SteraTransferBatchRepository;
import com.cupit.service.EmployeeService;
import com.cupit.service.JftdReportDataService;
import com.cupit.service.MemberInfoService;
import com.cupit.service.SettlementFeeRateService;
import com.cupit.service.SettlementItemCodeService;
import com.cupit.service.TransferFeeRateService;

/**
 * MenuController のテスト。会員情報照会(/member_info)・
 * 店舗・端末・SMCC加盟店番号情報照会(/store_terminal_smcc)のルーティングを検証する。
 * Service／Repositoryをモック化し、データベースに依存せず動作を検証する。
 */
@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    private static final String DEFAULT_TRADE_CODE = "01-001";
    private static final String VIEW_NAME_MEMBER_INFO = "member_info";
    private static final String VIEW_NAME_STORE_TERMINAL_SMCC = "store_terminal_smcc";
    private static final String VIEW_NAME_JFTD_TRANSFER = "jftd_transfer";

    @Mock
    private MemberInfoService memberInfoService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private JftdReportDataService jftdReportDataService;

    @Mock
    private SteraStoreRepository steraStoreRepository;

    @Mock
    private SteraTerminalRepository steraTerminalRepository;

    @Mock
    private SmccMerchantNoRepository smccMerchantNoRepository;

    @Mock
    private SettlementFeeRateService settlementFeeRateService;

    @Mock
    private SettlementItemCodeService settlementItemCodeService;

    @Mock
    private TransferFeeRateService transferFeeRateService;

    @Mock
    private JftdTransferBatchRepository jftdTransferBatchRepository;

    @Mock
    private SteraTransferBatchRepository steraTransferBatchRepository;

    private MenuController menuController;

    @BeforeEach
    void setUp() {
        menuController = new MenuController(
                memberInfoService, employeeService, jftdReportDataService,
                steraStoreRepository, steraTerminalRepository, smccMerchantNoRepository,
                settlementFeeRateService, settlementItemCodeService, transferFeeRateService,
                jftdTransferBatchRepository, steraTransferBatchRepository);
    }

    @Test
    void memberInfoAddsInfoAttributeWhenFound() {
        MemberInfo expected = new MemberInfo();
        expected.setTradeCode(DEFAULT_TRADE_CODE);
        expected.setStoreName("赤坂生花店");
        when(memberInfoService.findByTradeCode(DEFAULT_TRADE_CODE)).thenReturn(expected);
        Model model = new ExtendedModelMap();

        String viewName = menuController.memberInfo(DEFAULT_TRADE_CODE, model);

        assertThat(viewName).isEqualTo(VIEW_NAME_MEMBER_INFO);
        assertThat(model.getAttribute("info")).isEqualTo(expected);
    }

    @Test
    void memberInfoSetsNullInfoWhenNotFound() {
        when(memberInfoService.findByTradeCode("99-999"))
                .thenThrow(new MemberInfoNotFoundException("会員情報が見つかりません: 99-999"));
        Model model = new ExtendedModelMap();

        String viewName = menuController.memberInfo("99-999", model);

        assertThat(viewName).isEqualTo(VIEW_NAME_MEMBER_INFO);
        assertThat(model.getAttribute("info")).isNull();
    }

    @Test
    void memberInfoSetsNullInfoWhenTradeCodeParameterOmitted() {
        when(memberInfoService.findByTradeCode(null))
                .thenThrow(new MemberInfoNotFoundException("会員情報が見つかりません: "));
        Model model = new ExtendedModelMap();

        String viewName = menuController.memberInfo(null, model);

        assertThat(viewName).isEqualTo(VIEW_NAME_MEMBER_INFO);
        assertThat(model.getAttribute("info")).isNull();
    }

    @Test
    void storeTerminalSmccAddsStoreTerminalsAndMerchantNumbersWhenFound() {
        SteraStore store = new SteraStore();
        store.setTradeCode(DEFAULT_TRADE_CODE);
        store.setStoreName("赤坂生花店");
        when(steraStoreRepository.findByTradeCode(DEFAULT_TRADE_CODE))
                .thenReturn(Optional.of(store));

        SteraTerminal terminal = new SteraTerminal();
        terminal.setTradeCode(DEFAULT_TRADE_CODE);
        terminal.setTerminalId("TERM0000001");
        terminal.setBranchCode("01-001000");
        terminal.setTerminalStatus("利用中");
        terminal.setTerminalStartDate(LocalDate.of(2020, 1, 1));
        List<SteraTerminal> terminals = List.of(terminal);
        when(steraTerminalRepository.findByTradeCodeOrderByRecordNoAsc(DEFAULT_TRADE_CODE))
                .thenReturn(terminals);

        SmccMerchantNo merchant = new SmccMerchantNo();
        merchant.setTradeCode(DEFAULT_TRADE_CODE);
        merchant.setMerchantNo("12345678");
        merchant.setType("クレジット");
        merchant.setBranchCode("01-001000");
        List<SmccMerchantNo> merchantNumbers = List.of(merchant);
        when(smccMerchantNoRepository.findByTradeCodeOrderByRecordNoAsc(DEFAULT_TRADE_CODE))
                .thenReturn(merchantNumbers);

        Model model = new ExtendedModelMap();

        String viewName = menuController.storeTerminalSmcc(DEFAULT_TRADE_CODE, model);

        assertThat(viewName).isEqualTo(VIEW_NAME_STORE_TERMINAL_SMCC);
        assertThat(model.getAttribute("store")).isEqualTo(store);
        assertThat(model.getAttribute("terminals")).isEqualTo(terminals);
        assertThat(model.getAttribute("merchantNumbers")).isEqualTo(merchantNumbers);
    }

    @Test
    void storeTerminalSmccSetsEmptyListsAndSkipsLookupsWhenStoreNotFound() {
        when(steraStoreRepository.findByTradeCode("99-999")).thenReturn(Optional.empty());
        Model model = new ExtendedModelMap();

        String viewName = menuController.storeTerminalSmcc("99-999", model);

        assertThat(viewName).isEqualTo(VIEW_NAME_STORE_TERMINAL_SMCC);
        assertThat(model.getAttribute("store")).isNull();
        assertThat(model.getAttribute("terminals")).isEqualTo(List.of());
        assertThat(model.getAttribute("merchantNumbers")).isEqualTo(List.of());
        verifyNoInteractions(steraTerminalRepository, smccMerchantNoRepository);
    }

    @Test
    void storeTerminalSmccSetsEmptyListsWhenTradeCodeParameterOmitted() {
        when(steraStoreRepository.findByTradeCode(null)).thenReturn(Optional.empty());
        Model model = new ExtendedModelMap();

        String viewName = menuController.storeTerminalSmcc(null, model);

        assertThat(viewName).isEqualTo(VIEW_NAME_STORE_TERMINAL_SMCC);
        assertThat(model.getAttribute("store")).isNull();
        assertThat(model.getAttribute("terminals")).isEqualTo(List.of());
        assertThat(model.getAttribute("merchantNumbers")).isEqualTo(List.of());
        verifyNoInteractions(steraTerminalRepository, smccMerchantNoRepository);
    }

    @Test
    void jftdTransferAddsLatestConfirmedBatchIdsWhenPresent() {
        JftdTransferBatch jftdBatch = new JftdTransferBatch();
        jftdBatch.setTransferBatchId(3);
        when(jftdTransferBatchRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(jftdBatch));

        SteraTransferBatch steraBatch = new SteraTransferBatch();
        steraBatch.setTransferBatchId(5);
        when(steraTransferBatchRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.of(steraBatch));

        Model model = new ExtendedModelMap();

        String viewName = menuController.jftdTransfer(model);

        assertThat(viewName).isEqualTo(VIEW_NAME_JFTD_TRANSFER);
        assertThat(model.getAttribute("latestJftdTransferBatchId")).isEqualTo(3);
        assertThat(model.getAttribute("latestSteraTransferBatchId")).isEqualTo(5);
    }

    @Test
    void jftdTransferSetsNullLatestBatchIdsWhenNoneConfirmedYet() {
        when(jftdTransferBatchRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.empty());
        when(steraTransferBatchRepository.findFirstByOrderByCreatedAtDesc())
                .thenReturn(Optional.empty());

        Model model = new ExtendedModelMap();

        String viewName = menuController.jftdTransfer(model);

        assertThat(viewName).isEqualTo(VIEW_NAME_JFTD_TRANSFER);
        assertThat(model.getAttribute("latestJftdTransferBatchId")).isNull();
        assertThat(model.getAttribute("latestSteraTransferBatchId")).isNull();
    }

}
