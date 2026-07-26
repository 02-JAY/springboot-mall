package com.jay.springbootmall.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jay.springbootmall.member.dto.LoginRequest;
import com.jay.springbootmall.member.dto.MemberResponse;
import com.jay.springbootmall.member.dto.RegisterRequest;
import com.jay.springbootmall.member.model.Member;
import com.jay.springbootmall.member.service.MemberService;
import com.jay.springbootmall.util.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberClientController.class) // 指向 MemberClientController
@AutoConfigureMockMvc(addFilters = false) // 關閉測試時的 Security 過濾器
public class MemberClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtUtils jwtUtils; // 因 Controller 有注入 JwtUtils，這裡也要 Mock 起來

    // ➕ 加上這行，假造 JPA Mapping Context 避開檢查
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("測試會員註冊 - 成功 (201 Created)")
    public void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");

        MemberResponse mockResponse = new MemberResponse(
                1L,
                "test@example.com",
                null,
                1,
                LocalDateTime.now(),
                Set.of("ROLE_MEMBER")
        );

        when(memberService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // API 正確路徑：/api/v1/members/register
        mockMvc.perform(post("/api/v1/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // 註冊是 201 Created
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("測試會員登入 - 成功 (200 OK，包含 Token 與 memberId)")
    public void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");

        Member mockMember = new Member();
        mockMember.setId(1L);
        mockMember.setEmail("test@example.com");
        mockMember.setRoles(new HashSet<>()); // 防止 NPE

        when(memberService.login(any(LoginRequest.class))).thenReturn(mockMember);
        when(jwtUtils.generateToken(anyLong(), anyString(), anySet())).thenReturn("mocked-jwt-token");

        // API 正確路徑：/api/v1/members/login
        mockMvc.perform(post("/api/v1/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.memberId").value(1));
    }

    @Test
    @DisplayName("測試查詢個人資料詳情 - 成功 (200 OK)")
    public void getMemberProfile_Success() throws Exception {
        MemberResponse mockResponse = new MemberResponse(
                1L,
                "test@example.com",
                "U1234567890abcdef1234567890abcdef",
                1,
                LocalDateTime.now(),
                Set.of("ROLE_MEMBER")
        );

        when(memberService.getMemberById(1L)).thenReturn(mockResponse);

        // API 正確路徑：/api/v1/members/1
        mockMvc.perform(get("/api/v1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.lineUserId").value("U1234567890abcdef1234567890abcdef"));
    }
}