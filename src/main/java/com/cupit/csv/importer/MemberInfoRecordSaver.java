package com.cupit.csv.importer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * MemberInfo を1件ずつ独立したトランザクションで保存する。
 * REQUIRES_NEWで呼び出し元のトランザクションから分離することで、DB制約違反
 * （桁数超過等）が1件発生しても、その行の保存だけが単独でロールバックされ、
 * 呼び出し元（MemberInfoFileImporter）が処理済みの他の行の登録には影響しない。
 */
@Component
public class MemberInfoRecordSaver {

    private final MemberInfoRepository memberInfoRepository;

    public MemberInfoRecordSaver(MemberInfoRepository memberInfoRepository) {
        this.memberInfoRepository = memberInfoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(MemberInfo record) {
        memberInfoRepository.save(record);
    }

}
