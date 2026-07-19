package com.jay.springbootmall.member.controller;

import com.jay.springbootmall.member.dto.LoginRequest;
import com.jay.springbootmall.member.dto.MemberResponse;
import com.jay.springbootmall.member.dto.RegisterRequest;
import com.jay.springbootmall.member.model.Member;
import com.jay.springbootmall.member.service.MemberService;
import com.jay.springbootmall.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.catalina.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "會員前台 API", description = "提供一般使用者註冊、個人資料管理、安全設定等功能")
@RestController
@RequestMapping("/api/v1/members") // 統一 v1 版本管理
public class MemberClientController {

    private final MemberService memberService;
    private final JwtUtils jwtUtils; // 2. 宣告私有變數

    // 遵循現代化建構子注入，不使用 @Autowired 變數注入
    public MemberClientController(MemberService memberService, JwtUtils jwtUtils) {
        this.memberService = memberService;
        this.jwtUtils = jwtUtils;
    }
    @Operation(summary = "會員註冊", description = "提供新使用者透過 Email 進行商城帳號註冊。註冊成功後預設為啟用狀態，且尚未綁定 LINE ID。")
    @PostMapping("/register")
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody RegisterRequest request) {
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

    @Operation(summary = "會員登入", description = "使用者輸入 Email 與密碼進行驗證。驗證成功後，後端會回傳 JWT 通行證給前端。")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {

        // 1. 呼叫 Service 進行密碼驗證，並取得登入成功的 Member Entity
        // (注意：這裡直接讓 Service 回傳 Member Entity，方便我們在 Controller 直接拿 id、email 與 roles)
        Member member = memberService.login(request);

        // 2. 動態將該會員擁有的 Roles 轉為 String Set (例如：["ROLE_MEMBER", "ROLE_ADMIN"])
        Set<String> roles = member.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toSet());

        // 3. 帶入該會員的「真實 ID」與「Email」產生 Token
        String jwtToken = jwtUtils.generateToken(member.getId(), member.getEmail(), roles);

        // 4. 回傳 token 與真實的 memberId 給前端
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("memberId", member.getId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "LINE OAuth 回傳進入點 (Callback)", description = "接收 LINE 重新導向取得的臨時 code 與 state（內含 memberId），由後端自動向 LINE 換取真實 User ID 並完成綁定。")
    @GetMapping("/callback") // 必須是 GET，且路徑與 LINE 後台設定完全一致
    public ResponseEntity<MemberResponse> lineCallback(
            @RequestParam String code,
            @RequestParam String state) {

        // 1. 從 state 參數中還原出你的商城會員 ID
        // 註：如果你前端傳入的是純數字字串，這裡直接轉成 Long
        Long memberId = Long.parseLong(state);

        // 2. 呼叫你原本寫在 Service 的一條龍自動化邏輯
        // Service 內部會動態讀取 application.properties 的 ClientID/Secret/Uri
        // 去跟 LINE 換 Token -> 解析出 lineId -> 自動寫入該 memberId 的 MySQL 欄位
        MemberResponse response = memberService.processLineBinding(memberId, code);

        return ResponseEntity.ok(response);
    }
}
