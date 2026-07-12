package com.jay.springbootmall.member.controller;

import com.jay.springbootmall.member.dto.MemberResponse;
import com.jay.springbootmall.member.dto.RegisterRequest;
import com.jay.springbootmall.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "會員前台 API", description = "提供一般使用者註冊、個人資料管理、安全設定等功能")
@RestController
@RequestMapping("/api/v1/members") // 統一 v1 版本管理
public class MemberClientController {

    private final MemberService memberService;

    // 遵循現代化建構子注入，不使用 @Autowired 變數注入
    public MemberClientController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "會員註冊", description = "提供新使用者透過 Email 進行商城帳號註冊。註冊成功後預設為啟用狀態，且尚未綁定 LINE ID。")
    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@RequestBody RegisterRequest request) {
        MemberResponse response = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "取得個人資料詳情", description = "根據會員主鍵 ID 查詢完整的個人帳號資訊、狀態與擁有的角色權限。")
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberProfile(@PathVariable Long id) {
        MemberResponse response = memberService.getMemberById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "修改會員密碼", description = "使用者變更密碼時使用。會先透過 BCrypt 驗證舊密碼是否正確，通過後才可寫入新密碼雜湊。")
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        memberService.updatePassword(id, oldPassword, newPassword);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "連動綁定 LINE 帳號", description = "當使用者於網站內點擊聯繫我們並授權同意後，將解析出的真實 LINE User ID 傳入完成帳號連動。")
    @PostMapping("/{id}/bind-line")
    public ResponseEntity<MemberResponse> bindLineUserId(
            @PathVariable Long id,
            @RequestParam String lineUserId) {
        MemberResponse response = memberService.bindLineUserId(id, lineUserId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
