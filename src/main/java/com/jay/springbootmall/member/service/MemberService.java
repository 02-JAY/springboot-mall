package com.jay.springbootmall.member.service;

import com.jay.springbootmall.member.dto.LoginRequest;
import com.jay.springbootmall.member.dto.MemberResponse;
import com.jay.springbootmall.member.dto.RegisterRequest;
import com.jay.springbootmall.member.model.Member;

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

    // ：登入驗證方法，成功則回傳會員資料，失敗則拋出異常
    Member login(LoginRequest request);
}
