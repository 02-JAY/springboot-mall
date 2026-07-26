package com.jay.springbootmall.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "會員登入請求參數")
public class LoginRequest {

    @Schema(description = "會員註冊的電子信箱", example = "test@gmail.com")
    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @Schema(description = "會員密碼", example = "password123")
    @NotBlank(message = "密碼不能為空")
    private String password;

    // --- Getter and Setter ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
