package com.cupit.dto;

import java.util.List;

import com.cupit.service.settlement.SteraTransferLineItem;

/**
 * その他統合振込CSV作成のプレビューAPI（{@code /stera_transfer/preview}）のレスポンス。
 * 確定前に確認できるよう、取引コード単位の明細（{@code lineItems}）に加えて、
 * 確定対象となるファイル一覧（{@code targetFiles}）を返す。
 */
public class SteraTransferPreviewResponse {

    private final List<SteraTransferLineItem> lineItems;

    private final List<TransferTargetFile> targetFiles;

    public SteraTransferPreviewResponse(
            List<SteraTransferLineItem> lineItems, List<TransferTargetFile> targetFiles) {
        this.lineItems = lineItems;
        this.targetFiles = targetFiles;
    }

    public List<SteraTransferLineItem> getLineItems() {
        return lineItems;
    }

    public List<TransferTargetFile> getTargetFiles() {
        return targetFiles;
    }

}
