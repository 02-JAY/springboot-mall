package com.jay.springbootmall.member.repository;

import com.jay.springbootmall.member.model.MemberSecurity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSecurityRepository extends JpaRepository<MemberSecurity, Long> {
}
