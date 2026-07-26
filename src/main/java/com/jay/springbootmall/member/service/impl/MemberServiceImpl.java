package com.jay.springbootmall.member.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── 核心修改 1：改由 application.properties 動態注入，上版不留機密 ───
    @Value("${line.auth.client-id}")
    private String lineClientId;

    @Value("${line.auth.client-secret}")
    private String lineClientSecret;

    @Value("${line.auth.redirect-uri}")
    private String lineRedirectUri;

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
     * 【Update】核心綁定邏輯：整合 LINE OAuth 流程
     */
    @Override
    @Transactional
    public MemberResponse processLineBinding(Long memberId, String code) {
        // 1. 遠端請求 LINE 伺服器，利用臨時 code 交換真實的 lineUserId
        String lineUserId = fetchLineUserIdFromLineServer(code);

        if (lineUserId == null || lineUserId.trim().isEmpty()) {
            throw new IllegalOperationException("無法從 LINE 伺服器取得用戶資訊，請重新登入授權");
        }

        // 2. 將得到的 lineUserId 傳入你原本寫好的安全校驗方法，完成寫入
        return this.bindLineUserId(memberId, lineUserId);
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
                .orElseThrow(() -> new IllegalOperationException("登入失敗：帳號或密碼錯誤"));

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


    /**
     * 遠端發送請求至 LINE 伺服器，利用授權碼 (authorization_code) 換取 Access Token 與 ID Token，
     * 並解密 id_token 中的 Payload 取得用戶的唯一識別碼 (lineUserId)。
     *
     * @param code LINE OAuth 授權完成後回傳的臨時授權碼
     * @return String LINE 用戶唯一識別碼 (sub / lineUserId)
     * @throws IllegalOperationException 當與 LINE 伺服器通訊失敗、回傳格式不符或解析異常時拋出
     */
    private String fetchLineUserIdFromLineServer(String code) {
        // 建立 HTTP 請求工具與 JSON 解析器
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 1. 定義 LINE Token API 端點
            String tokenUrl = "https://api.line.me/oauth2/v2.1/token";

            // 2. 設定 HTTP Header (LINE 規定必須為 application/x-www-form-urlencoded)
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 3. 組裝 Request Body 參數 (包含授權類型、Code、Redirect URI 與憑證)
            MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
            tokenBody.add("grant_type", "authorization_code");
            tokenBody.add("code", code);
            tokenBody.add("redirect_uri", lineRedirectUri);
            tokenBody.add("client_id", lineClientId);
            tokenBody.add("client_secret", lineClientSecret);

            // 4. 打包請求並發送 POST 至 LINE 伺服器
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, String.class);

            // 防護 A：檢查 HTTP 狀態碼是否為 20x 成功狀態，且 Body 有回應
            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
                throw new IllegalOperationException("向 LINE 驗證失敗，無法取得 Token");
            }

            // 5. 解析 LINE 回傳的 JSON 回應
            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            JsonNode idTokenNode = tokenJson.get("id_token");

            // 防護 B：確認是否有拿到 OpenID Connect 的 id_token (JWT 格式字串)
            if (idTokenNode == null || idTokenNode.asText().isEmpty()) {
                throw new IllegalOperationException("LINE 回傳資料異常，缺乏 id_token");
            }

            // 6. 解開 JWT 結構 (JWT 由 Header.Payload.Signature 三段以點號分隔組成)
            String[] jwtParts = idTokenNode.asText().split("\\.");
            if (jwtParts.length < 2) {
                throw new IllegalOperationException("LINE id_token 格式不正確");
            }

            // 7. Base64 解碼中間段 (Payload) 取得 JSON 字串
            String payloadJson = new String(Base64.getUrlDecoder().decode(jwtParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payloadJson);

            // 8. 取出 "sub" 欄位 (Subject，在 OpenID 規範中代表該用戶於 LINE 的唯一 ID)
            return payloadNode.has("sub") ? payloadNode.get("sub").asText() : null;

        } catch (Exception e) {
            // 若過程中有任何連線、解析或轉型異常，統一包裝為業務例外拋出
            // 建議專案引入 @Slf4j 後，加上：log.error("LINE OAuth 流程發生錯誤", e);
            throw new IllegalOperationException("LINE 帳號授權失敗：" + e.getMessage());
        }
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