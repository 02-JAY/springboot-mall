package com.jay.springbootmall.member.service;

import com.jay.springbootmall.exception.IllegalOperationException;
import com.jay.springbootmall.exception.ResourceNotFoundException;
import com.jay.springbootmall.member.dto.LoginRequest;
import com.jay.springbootmall.member.dto.MemberResponse;
import com.jay.springbootmall.member.dto.RegisterRequest;
import com.jay.springbootmall.member.model.Member;
import com.jay.springbootmall.member.model.MemberSecurity;
import com.jay.springbootmall.member.model.Role;
import com.jay.springbootmall.member.repository.MemberRepository;
import com.jay.springbootmall.member.repository.RoleRepository;
import com.jay.springbootmall.member.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member mockMember;
    private MemberSecurity mockSecurity;

    @BeforeEach
    void setUp() {
        // 初始化共用的 Member 物件
        mockMember = new Member();
        mockMember.setId(1L);
        mockMember.setEmail("test@example.com");
        mockMember.setStatus(1);
        mockMember.setCreatedAt(LocalDateTime.now());
        mockMember.setRoles(new HashSet<>());

        mockSecurity = new MemberSecurity();
        mockSecurity.setPasswordHash("hashed_password");
        mockMember.setSecurity(mockSecurity);
    }

    // ─────────────────────────────────────────────────────────────
    // 1. 註冊測試 (register)
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("註冊會員 - 成功流程")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("123456");

        Role role = new Role();
        role.setRoleName("ROLE_MEMBER");

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");
        when(roleRepository.findByRoleName("ROLE_MEMBER")).thenReturn(Optional.of(role));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setId(1L);
            return member;
        });

        MemberResponse response = memberService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("newuser@example.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_MEMBER"));
    }

    @Test
    @DisplayName("註冊會員 - Email 已存在，拋出 IllegalOperationException")
    void register_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockMember));

        assertThrows(IllegalOperationException.class, () -> memberService.register(request));
        verify(memberRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────
    // 2. 登入測試 (login)
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("會員登入 - 成功流程")
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("123456");

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches("123456", "hashed_password")).thenReturn(true);

        Member result = memberService.login(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("會員登入 - 密碼錯誤，拋出 Exception")
    void login_WrongPassword_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong_password");

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> memberService.login(request));
    }

    // ─────────────────────────────────────────────────────────────
    // 3. 查詢與更新測試
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("查詢會員 - 找不到會員，拋出 ResourceNotFoundException")
    void getMemberById_NotFound_ThrowsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.getMemberById(99L));
    }

    @Test
    @DisplayName("修改密碼 - 舊密碼輸入錯誤，拋出 IllegalOperationException")
    void updatePassword_WrongOldPassword_ThrowsException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches("wrong_old", "hashed_password")).thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> memberService.updatePassword(1L, "wrong_old", "new_password"));
    }

    @Test
    @DisplayName("綁定 LINE 帳號 - 已被其他會員綁定，拋出 IllegalOperationException")
    void bindLineUserId_AlreadyBoundToAnotherMember_ThrowsException() {
        Member otherMember = new Member();
        otherMember.setId(2L); // 不同的 ID

        when(memberRepository.findByLineUserId("U123456")).thenReturn(Optional.of(otherMember));

        assertThrows(IllegalOperationException.class,
                () -> memberService.bindLineUserId(1L, "U123456"));
    }

    @Test
    @DisplayName("停用會員 - 成功將 status 改為 0")
    void disableMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(mockMember));

        memberService.disableMember(1L);

        assertEquals(0, mockMember.getStatus());
        verify(memberRepository, times(1)).save(mockMember);
    }
}