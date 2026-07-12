package com.jay.springbootmall.member.repository;

import com.jay.springbootmall.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 用於一般 Email 登入或檢查重複註冊
    Optional<Member> findByEmail(String email);

    // 用於 LINE 第三方登入
    Optional<Member> findByLineUserId(String lineUserId);
}
