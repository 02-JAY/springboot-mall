package com.jay.springbootmall.member.service.impl;

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
import com.jay.springbootmall.member.service.MemberService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 【Create】註冊新會員
     */
    @Override
    @Transactional
    public MemberResponse register(RegisterRequest request) {
        // 1. 檢查 Email 是否已被註冊 -> 改用 IllegalOperationException (帳號已存在，業務衝突)
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalOperationException("該 Email 已被註冊");
        }

        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setLineUserId(null);
        member.setStatus(1);

        MemberSecurity security = new MemberSecurity();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        security.setPasswordHash(hashedPassword);
        member.setSecurity(security);

        // 4. 給予預設角色 (ROLE_MEMBER) -> 改用 ResourceNotFoundException (如果資料庫少塞角色資料)
        Set<Role> defaultRoles = new HashSet<>();
        Role defaultRole = roleRepository.findByRoleName("ROLE_MEMBER")
                .orElseThrow(() -> new ResourceNotFoundException("系統錯誤：找不到預設角色 ROLE_MEMBER"));
        defaultRoles.add(defaultRole);
        member.setRoles(defaultRoles);

        Member savedMember = memberRepository.save(member);

        return convertToResponse(savedMember);
    }

    /**
     * 【Read】透過 ID 查詢會員
     */
    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {
        // 2. 找不到會員 -> 改用 ResourceNotFoundException (資料不存在)
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該會員，ID: " + id));
        return convertToResponse(member);
    }

    /**
     * 【Update】綁定 LINE 帳號
     */
    @Override
    @Transactional
    public MemberResponse bindLineUserId(Long memberId, String lineUserId) {
        if (lineUserId == null || lineUserId.trim().isEmpty()) {
            throw new IllegalOperationException("無效的 LINE User ID");
        }

        // 確保此 LINE 帳號沒有被系統中的其他會員綁定過 -> 改用 IllegalOperationException
        memberRepository.findByLineUserId(lineUserId).ifPresent(m -> {
            if (!m.getId().equals(memberId)) {
                throw new IllegalOperationException("該 LINE 帳號已被其他會員綁定");
            }
        });

        // 找不到會員 -> 改用 ResourceNotFoundException
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該會員，ID: " + memberId));

        member.setLineUserId(lineUserId);
        Member updatedMember = memberRepository.save(member);
        return convertToResponse(updatedMember);
    }

    /**
     * 【Update】修改密碼
     */
    @Override
    @Transactional
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        // 找不到會員 -> 改用 ResourceNotFoundException
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該會員，ID: " + id));

        MemberSecurity security = member.getSecurity();

        // 舊密碼輸入錯誤 -> 改用 IllegalOperationException (密碼比對失敗屬於非法操作/操作錯誤)
        if (!passwordEncoder.matches(oldPassword, security.getPasswordHash())) {
            throw new IllegalOperationException("舊密碼輸入錯誤");
        }

        security.setPasswordHash(passwordEncoder.encode(newPassword));
        security.setPasswordChangedAt(LocalDateTime.now());

        memberRepository.save(member);
    }

    /**
     * 【Delete】停用會員帳號（軟刪除）
     */
    @Override
    @Transactional
    public void disableMember(Long id) {
        // 找不到會員 -> 改用 ResourceNotFoundException
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該會員，ID: " + id));

        member.setStatus(0);
        memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true) // 確保能 Lazily 載入關聯的 Security
    public Member login(LoginRequest request) {
        // 1. 根據 Email 查出會員 (此時會一併載入 roles，因為 roles 是 EAGER)
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("登入失敗：帳號或密碼錯誤"));

        // 2. 檢查帳號啟用狀態
        if (member.getStatus() != 1) {
            throw new IllegalStateException("該帳號已被停用，請聯繫管理員");
        }

        // 3. 從 1:1 關聯的 Security 物件中取出密碼雜湊值進行 BCrypt 比對
        MemberSecurity security = member.getSecurity();
        if (security == null || security.getPasswordHash() == null) { // 假設密碼欄位叫 passwordHash
            throw new IllegalArgumentException("登入失敗：帳號安全資料異常");
        }

        if (!passwordEncoder.matches(request.getPassword(), security.getPasswordHash())) {
            throw new IllegalArgumentException("登入失敗：帳號或密碼錯誤");
        }

        return member;
    }

    private MemberResponse convertToResponse(Member member) {
        Set<String> roleNames = member.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getLineUserId(),
                member.getStatus(),
                member.getCreatedAt(),
                roleNames
        );
    }

}