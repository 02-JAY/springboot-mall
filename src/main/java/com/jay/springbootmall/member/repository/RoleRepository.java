package com.jay.springbootmall.member.repository;

import com.jay.springbootmall.member.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // 透過角色名稱（如 "ROLE_USER"）尋找角色
    Optional<Role> findByRoleName(String roleName);
}
