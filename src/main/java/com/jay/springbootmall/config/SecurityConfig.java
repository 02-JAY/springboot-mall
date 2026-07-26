package com.jay.springbootmall.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 注入寫好的過濾器
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 因採用 RESTful API 與 JWT，關閉 CSRF 防護（前後端分離必關）
                .csrf(csrf -> csrf.disable())

                // 2. 設定 Session 機制為無狀態（Stateless）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 網址與調度器權限精準控管
                .authorizeHttpRequests(auth -> auth
                        // 💡 關鍵：允許錯誤調度（DispatcherType.ERROR）與預設錯誤路徑放行
                        // 這能確保 @Valid 驗證失敗或 Service 拋出異常時，能正常轉發並回傳 ExceptionHandler 的內容，而不吃 403
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()

                        // 🔓 【完全公開放行】
                        .requestMatchers("/api/v1/members/register").permitAll() // 會員註冊
                        .requestMatchers("/api/v1/members/login").permitAll()    // 會員登入
                        .requestMatchers("/api/v1/products/**").permitAll()      // 商品搜尋、詳情、分類
                        .requestMatchers("/api/v1/bot/**").permitAll()           // LINE Bot 推薦 API
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger 介面

                        // 🔐 【管理員後台專屬限制】
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 👤 【一般會員前台專屬限制】
                        .requestMatchers("/api/v1/members/**").hasRole("MEMBER")

                        // 🛑 其餘所有請求，通通必須通過驗證
                        .anyRequest().authenticated()
                );

        // 關鍵：把 JwtAuthenticationFilter 加在標準的 UsernamePasswordAuthenticationFilter 之前
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
