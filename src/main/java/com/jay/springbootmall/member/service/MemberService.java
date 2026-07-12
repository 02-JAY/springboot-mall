package com.jay.springbootmall.member.service;

import com.jay.springbootmall.member.dto.MemberResponse;
import com.jay.springbootmall.member.dto.RegisterRequest;

public interface MemberService {
    // 【Create】註冊新會員
    MemberResponse register(RegisterRequest request);

    // 【Read】透過 ID 查詢會員
    MemberResponse getMemberById(Long id);

    // 【Update】綁定 LINE 帳號
    MemberResponse bindLineUserId(Long memberId, String lineUserId);

    // 【Update】修改密碼
    void updatePassword(Long id, String oldPassword, String newPassword);

    // 【Delete】停用會員帳號（軟刪除）
    void disableMember(Long id);
}
