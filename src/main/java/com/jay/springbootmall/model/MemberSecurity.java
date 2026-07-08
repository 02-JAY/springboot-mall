package com.jay.springbootmall.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "member_security")
public class MemberSecurity {

    @Id
    private Long memberId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // 透過 @MapsId 讓這張表的 PK (memberId) 直接作為外鍵對應 Member 的 PK
    @OneToOne
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    // 預設建構子
    public MemberSecurity() {
    }

    // 手動 Getters 與 Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
