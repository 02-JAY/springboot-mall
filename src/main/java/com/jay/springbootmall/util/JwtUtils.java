package com.jay.springbootmall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.Date;

@Component
public class JwtUtils {

    // 實務上放在 application.properties
    private final SecretKey SECRET_KEY;
    // Token 有效時間設定為 24 小時
    private final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // 💡 透過建構子注入 properties 中的 jwt.secret 設定
    public JwtUtils(@Value("${jwt.secret}") String secretKeyString) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 生成 JWT Token
     */
    public String generateToken(Long memberId, String email, Set<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(email) // 把 email 當作 subject
                .claim("memberId", memberId) // 夾帶會員 ID
                .claim("roles", roles)       // 夾帶角色權限
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析並驗證 JWT Token，提取出 Claims (裡面的資料)
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
