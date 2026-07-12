package com.jay.springbootmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 因採用 RESTful API，關閉 CSRF 防護（若後續使用 JWT，這是一定要關閉的）
                .csrf(csrf -> csrf.disable())

                // 2. 設定 Session 機制為無狀態（適用於 JWT 模式，若是傳統 Session 可移除此行）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 網址權限控制精準控管
                .authorizeHttpRequests(auth -> auth
                        // 🔓 【完全公開放行】
                        .requestMatchers("/api/v1/members/register").permitAll() // 會員註冊
                        .requestMatchers("/api/v1/products/**").permitAll()      // 商品搜尋、詳情、分類
                        .requestMatchers("/api/v1/bot/**").permitAll()           // LINE Bot 推薦 API
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger 介面

                        // 🔐 【管理員後台專屬限制】
                        // 只要網址帶有 /admin/ 結尾的所有路徑，強製檢查必須擁有 ROLE_ADMIN 角色
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 👤 【一般會員前台專屬限制】
                        // 排除掉 register 後，剩餘的 /api/v1/members/** 網址皆需 ROLE_MEMBER 權限
                        .requestMatchers("/api/v1/members/**").hasRole("MEMBER")

                        // 🛑 其餘所有請求，通通必須通過驗證
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
