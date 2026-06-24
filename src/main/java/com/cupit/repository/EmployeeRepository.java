package com.cupit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cupit.model.Employee;

/**
 * 社員マスタ (m_employee) の永続化を担うリポジトリ。
 * 主キーはユーザー ID (user_id) を表す String 型とする。
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    /**
     * メールアドレスを指定して社員を 1 件取得する。
     *
     * @param email メールアドレス
     * @return 該当する社員。存在しない場合は空
     */
    Optional<Employee> findByEmail(String email);

    /**
     * 全社員をユーザー ID の昇順で取得する。
     *
     * @return 社員の一覧
     */
    List<Employee> findAllByOrderByUserIdAsc();

    /**
     * ユーザー ID が最大の社員を 1 件取得する。新規登録時の自動採番に使用する。
     *
     * @return ユーザー ID が最大の社員。社員が存在しない場合は空
     */
    Optional<Employee> findFirstByOrderByUserIdDesc();

    /**
     * 指定したメールアドレスの社員が存在するかどうかを返す。
     *
     * @param email メールアドレス
     * @return 存在する場合は true
     */
    boolean existsByEmail(String email);

    /**
     * 指定したユーザー ID 以外で、指定したメールアドレスの社員が存在するか返す。
     * 編集時に自分自身を除外してメールアドレスの重複を判定するために使用する。
     *
     * @param email メールアドレス
     * @param userId 除外するユーザー ID
     * @return 存在する場合は true
     */
    boolean existsByEmailAndUserIdNot(String email, String userId);

    /**
     * 指定した社員のパスワード誤り回数を更新する。
     *
     * @param userId 対象のユーザー ID
     * @param passwordErrorCount 設定するパスワード誤り回数
     */
    @Modifying
    @Query("update Employee employee"
            + " set employee.passwordErrorCount = :passwordErrorCount"
            + " where employee.userId = :userId")
    void updatePasswordErrorCount(
            @Param("userId") String userId,
            @Param("passwordErrorCount") int passwordErrorCount);
}
