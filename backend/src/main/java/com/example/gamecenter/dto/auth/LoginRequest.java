package com.example.gamecenter.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 用户登录请求体（username 字段可填邮箱）。 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名或邮箱不能为空")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
