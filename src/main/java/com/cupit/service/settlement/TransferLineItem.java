package com.cupit.service.settlement;

/**
 * 統合振込CSVの1行（項目コード,取引コード,数量,金額）に対応するDTO。
 * amountは現時点では支払金額①（決済事業者手数料控除後、弊社手数料控除前）であり、
 * 手数料②が未実装のため最終的な振込金額（支払金額②）ではない
 * （調査メモ「論点・オープン事項」項番6を参照）。統合振込CSV自体はamountのみを使う。
 * grossAmount・acquirerFeeTaxFree・acquirerFeeBase・acquirerFeeTaxは帳票
 * （売上報告書・支払明細書）の「決済金額合計」「事業者手数料内訳」列のためだけに保持する
 * （CSVの内容・符号には影響しない）。スマレジ(端末月額利用料)のように、amountが
 * 「売上から差し引く決済手数料相当額」を表す行は、grossAmount=0・手数料内訳=amount相当
 * として扱う（帳票上は事業者手数料差引後決済金額が負数になる）。
 */
public class TransferLineItem {

    private final String tradeCode;

    private final String itemCode;

    private final int quantity;

    private final int amount;

    private final int grossAmount;

    private final int acquirerFeeTaxFree;

    private final int acquirerFeeBase;

    private final int acquirerFeeTax;

    public TransferLineItem(
            String tradeCode, String itemCode, int quantity, int amount,
            int grossAmount, int acquirerFeeTaxFree, int acquirerFeeBase, int acquirerFeeTax) {
        this.tradeCode = tradeCode;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.amount = amount;
        this.grossAmount = grossAmount;
        this.acquirerFeeTaxFree = acquirerFeeTaxFree;
        this.acquirerFeeBase = acquirerFeeBase;
        this.acquirerFeeTax = acquirerFeeTax;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getAmount() {
        return amount;
    }

    public int getGrossAmount() {
        return grossAmount;
    }

    public int getAcquirerFeeTaxFree() {
        return acquirerFeeTaxFree;
    }

    public int getAcquirerFeeBase() {
        return acquirerFeeBase;
    }

    public int getAcquirerFeeTax() {
        return acquirerFeeTax;
    }

}
