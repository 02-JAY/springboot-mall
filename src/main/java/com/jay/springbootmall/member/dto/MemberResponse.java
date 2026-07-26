package com.jay.springbootmall.member.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class MemberResponse {
    private Long id;
    private String email;
    private String lineUserId;
    private Integer status;
    private LocalDateTime createdAt;
    private Set<String> roles; // 只回傳角色名稱字串陣列即可

    // 快速轉換的建構子
    public MemberResponse(Long id, String email, String lineUserId, Integer status, LocalDateTime createdAt, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.lineUserId = lineUserId;
        this.status = status;
        this.createdAt = createdAt;
        this.roles = roles;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getLineUserId() {
        return lineUserId;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<String> getRoles() {
        return roles;
    }
}

