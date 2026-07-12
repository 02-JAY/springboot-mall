package com.jay.springbootmall.member.controller;

import com.jay.springbootmall.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "會員管理後台 API", description = "提供後台管理人員審查、停用或調整會員狀態")
@RestController
@RequestMapping("/api/v1/admin/members") // 統一管理員路徑隔離
public class MemberAdminController {

    private final MemberService memberService;

    public MemberAdminController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "停用/封鎖會員帳號（軟刪除）", description = "管理端因安全或違規因素停用該會員。此操作為軟刪除，會將 status 改為 0，不會抹除實體歷史資料以利訂單追蹤。")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableMember(@PathVariable Long id) {
        memberService.disableMember(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
