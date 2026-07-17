package com.cupit.dto;

import java.util.List;

import com.cupit.service.settlement.ReportRow;

/**
 * JFTD統合振込CSV作成のプレビューAPI（{@code /jftd_transfer/preview}）のレスポンス。
 * 確定前に確認できるよう、決済会社×カードブランド単位の集計（{@code summary}）に加えて、
 * 確定対象となるファイル一覧（{@code targetFiles}）を返す。
 */
public class JftdTransferPreviewResponse {

    private final List<ReportRow> summary;

    private final List<TransferTargetFile> targetFiles;

    public JftdTransferPreviewResponse(List<ReportRow> summary, List<TransferTargetFile> targetFiles) {
        this.summary = summary;
        this.targetFiles = targetFiles;
    }

    public List<ReportRow> getSummary() {
        return summary;
    }

    public List<TransferTargetFile> getTargetFiles() {
        return targetFiles;
    }

}
