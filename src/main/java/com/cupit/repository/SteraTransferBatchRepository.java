package com.cupit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraTransferBatch;

public interface SteraTransferBatchRepository
        extends JpaRepository<SteraTransferBatch, Integer> {

    List<SteraTransferBatch> findAllByOrderByCreatedAtDesc();

    /**
     * JFTD・その他統合振込CSV作成画面（/jftd_transfer）の「CSV再ダウンロード」ボタン用。
     * 確定直後のCSVダウンロードに失敗しても、確定済みデータ自体は保存済みのため
     * 再確定なしで直近の確定分だけ取り出して再ダウンロードできるようにする。
     */
    Optional<SteraTransferBatch> findFirstByOrderByCreatedAtDesc();

}
