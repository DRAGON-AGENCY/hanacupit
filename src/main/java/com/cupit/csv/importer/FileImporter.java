package com.cupit.csv.importer;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.model.ImportBatch;

public interface FileImporter {

    /**
     * ファイルを解析してDBに登録する。データエラーが発生した行は登録せずスキップし、
     * ファイルの最後まで処理を続けたうえで、正常な行の登録件数と発生した全エラーを
     * ImportResult として返す（データエラーによってファイル全体をロールバックしない）。
     *
     * @param file  アップロードファイル
     * @param batch 取り込みバッチ（batch_id が設定済み）
     * @return 登録件数と全エラーを含むインポート結果
     * @throws IOException ファイル読み込みエラー
     */
    ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException;

    /**
     * ファイルの先頭データ行から、m_paygate_store_mapping の検索キーを取得する。
     * JCB→加盟店番号、スマレジ→端末識別番号、ネットスターズ→店舗コード、
     * 楽天ペイ→STORE_NO、住信SBI→加盟店ID を返す。
     *
     * @param file アップロードファイル
     * @return 検索キー文字列
     * @throws IOException ファイル読み込みエラー
     */
    default String extractLookupKey(MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("extractLookupKey は未実装です。");
    }

    /**
     * ファイルの全データ行から、{@link #extractLookupKey}と同じ種別の検索キーを
     * 出現順・重複除去したうえで一覧として取得する。同じ決済種別・同一内容のファイルが
     * 誤って再アップロードされた際に、確認ダイアログで「どの取引の重複か」を利用者が
     * 判断できるようにするために使用する。
     *
     * @param file アップロードファイル
     * @return 検索キー文字列の一覧（出現順、重複除去済み）
     * @throws IOException ファイル読み込みエラー
     */
    default List<String> extractAllLookupKeys(MultipartFile file) throws IOException {
        throw new UnsupportedOperationException("extractAllLookupKeys は未実装です。");
    }

    /**
     * 指定したバッチIDに紐づく明細データを削除する。エラーを含んだまま確定されずに
     * 残っている過去のインポートバッチを、訂正後のファイルで置き換える（洗い替える）
     * ために使用する。
     *
     * @param batchId 削除対象のバッチID
     */
    default void deleteBatchData(int batchId) {
        throw new UnsupportedOperationException("deleteBatchData は未実装です。");
    }
}
