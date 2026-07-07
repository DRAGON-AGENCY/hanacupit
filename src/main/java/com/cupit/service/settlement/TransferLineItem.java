package com.cupit.service.settlement;

/**
 * 統合振込CSVの1行（項目コード,取引コード,数量,金額）に対応するDTO。
 * amountは現時点では支払金額①（決済事業者手数料控除後、弊社手数料控除前）であり、
 * 手数料②が未実装のため最終的な振込金額（支払金額②）ではない
 * （調査メモ「論点・オープン事項」項番6を参照）。
 */
public class TransferLineItem {

    private final String tradeCode;

    private final String itemCode;

    private final int quantity;

    private final int amount;

    public TransferLineItem(String tradeCode, String itemCode, int quantity, int amount) {
        this.tradeCode = tradeCode;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.amount = amount;
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

}
