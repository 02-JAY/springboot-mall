package com.jay.springbootmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 停用 CSRF 防護（方便測試 Postman/Swagger 送出 POST/PUT 請求）
                .csrf(csrf -> csrf.disable())

                // 2. 設定路徑放行規則
                .authorizeHttpRequests(auth -> auth
                        // 將 Swagger 3 (springdoc) 所有的相關路徑加入白名單
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()

                        // 暫時將所有其他請求也放行，等測試完 Swagger 再加鎖（可選）
                        // 如果你想只放行 Swagger，其他還是要登入，就保持 .anyRequest().authenticated()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
