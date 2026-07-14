package com.cupit.service.settlement;

/**
 * その他統合振込CSV（stera terminal）の1行に対応するDTO。取引コード単位で、
 * stera JCB・stera code・steraクレジット3フォーマットの明細を合算した集計結果と、
 * m_stera_storeから取得した振込先口座情報を保持する。項目コード方式ではないため
 * {@link TransferLineItem}とは別のDTOとする。
 */
public class SteraTransferLineItem {

    private final String tradeCode;

    private final int grossAmount;

    private final int acquirerFee;

    private final int companyFee;

    private final int transferFee;

    private final int netAmount;

    private final String bankCode;

    private final String bankName;

    private final String bankBranchCode;

    private final String branchName;

    private final String accountType;

    private final String accountNo;

    private final String accountHolderKana;

    public SteraTransferLineItem(
            String tradeCode, int grossAmount, int acquirerFee, int companyFee,
            int transferFee, int netAmount, String bankCode, String bankName,
            String bankBranchCode, String branchName, String accountType,
            String accountNo, String accountHolderKana) {
        this.tradeCode = tradeCode;
        this.grossAmount = grossAmount;
        this.acquirerFee = acquirerFee;
        this.companyFee = companyFee;
        this.transferFee = transferFee;
        this.netAmount = netAmount;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.bankBranchCode = bankBranchCode;
        this.branchName = branchName;
        this.accountType = accountType;
        this.accountNo = accountNo;
        this.accountHolderKana = accountHolderKana;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public int getGrossAmount() {
        return grossAmount;
    }

    public int getAcquirerFee() {
        return acquirerFee;
    }

    public int getCompanyFee() {
        return companyFee;
    }

    public int getTransferFee() {
        return transferFee;
    }

    public int getNetAmount() {
        return netAmount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankBranchCode() {
        return bankBranchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getAccountHolderKana() {
        return accountHolderKana;
    }

}
