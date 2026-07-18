package com.jay.springbootmall.config;

import com.jay.springbootmall.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 從 HTTP Header 提取 Authorization 欄位
        String authHeader = request.getHeader("Authorization");

        // 2. 檢查是否有帶 Bearer Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去除 "Bearer " 字串

            try {
                // 3. 解析 Token
                Claims claims = jwtUtils.parseToken(token);
                String email = claims.getSubject();
                Long memberId = claims.get("memberId", Long.class);

                // 4. 解析角色權限字串列表，轉為 Spring Security 認可的 GrantedAuthority
                List<?> rolesRaw = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = rolesRaw.stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .collect(Collectors.toList());

                // 5. 封裝成 Spring Security 內部通用的 Authentication 物件
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                // 💡 把自定義的 memberId 塞進 details，方便後面 Controller 取用
                authentication.setDetails(memberId);

                // 6. 放入上下文，這代表目前請求「已通過身分驗證」！
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // Token 過期或竄改，不塞入安全上下文，直接放行交給後續 SecurityConfig 攔截回傳 403/401
                logger.error("JWT 驗證失敗: " + e.getMessage());
            }
        }

        // 繼續執行後續的 Filter 鏈
        filterChain.doFilter(request, response);
    }
}
