package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.constant.EmailVerificationPurpose;
import com.example.gamecenter.dto.auth.LoginRequest;
import com.example.gamecenter.dto.auth.RegisterRequest;
import com.example.gamecenter.dto.auth.SendEmailCodeRequest;
import com.example.gamecenter.entity.User;
import com.example.gamecenter.exception.BusinessException;
import com.example.gamecenter.service.EmailVerificationService;
import com.example.gamecenter.service.UserService;
import com.example.gamecenter.utils.JwtUtils;
import com.example.gamecenter.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** 用户端认证：邮箱验证码、注册、登录与登出。 */
@RestController
@RequestMapping(ApiConstants.Auth.BASE)
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final EmailVerificationService emailVerificationService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(
            UserService userService,
            JwtUtils jwtUtils,
            EmailVerificationService emailVerificationService,
            BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    /** 按用途校验邮箱是否已注册，再发送验证码。 */
    @PostMapping(ApiConstants.Auth.SEND_CODE)
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest request) {
        EmailVerificationPurpose purpose = EmailVerificationPurpose.fromValue(request.getPurpose());
        if (purpose == null) {
            return Result.error(
                    ApiBizError.AUTH_EMAIL_PURPOSE_INVALID.getCode(),
                    ApiBizError.AUTH_EMAIL_PURPOSE_INVALID.getMessage());
        }

        String email = request.getEmail().trim();
        if (purpose == EmailVerificationPurpose.REGISTER) {
            if (userService.findByEmail(email) != null) {
                return Result.error(
                        ApiBizError.AUTH_EMAIL_CONFLICT.getCode(),
                        ApiBizError.AUTH_EMAIL_CONFLICT.getMessage());
            }
        } else if (purpose == EmailVerificationPurpose.CHANGE_PASSWORD) {
            if (userService.findByEmail(email) == null) {
                return Result.error(
                        ApiBizError.AUTH_EMAIL_NOT_REGISTERED.getCode(),
                        ApiBizError.AUTH_EMAIL_NOT_REGISTERED.getMessage());
            }
        }

        try {
            emailVerificationService.sendCode(email, purpose);
            return Result.success("验证码已发送", null);
        } catch (BusinessException ex) {
            return Result.error(ex.getCode(), ex.getMessage());
        }
    }

    /** 校验邮箱验证码后创建用户（密码由 Service 层 BCrypt 编码）。 */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        if (!emailVerificationService.verifyAndConsume(
                request.getEmail(), EmailVerificationPurpose.REGISTER, request.getVerificationCode())) {
            return Result.error(
                    ApiBizError.AUTH_VERIFICATION_CODE_INVALID.getCode(),
                    ApiBizError.AUTH_VERIFICATION_CODE_INVALID.getMessage());
        }

        if (userService.findByEmail(request.getEmail()) != null) {
            return Result.error(
                    ApiBizError.AUTH_EMAIL_CONFLICT.getCode(),
                    ApiBizError.AUTH_EMAIL_CONFLICT.getMessage());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword());
        user.setEmail(request.getEmail());

        if (userService.register(user)) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            return Result.success("User registered successfully", data);
        }
        return Result.error(
                ApiBizError.AUTH_USERNAME_CONFLICT.getCode(),
                ApiBizError.AUTH_USERNAME_CONFLICT.getMessage());
    }

    /** 支持用户名或邮箱登录，签发 role=user 的 JWT。 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String loginId = request.getUsername().trim();
        User user = loginId.contains("@")
                ? userService.findByEmail(loginId)
                : userService.findByUsername(loginId);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return Result.error(
                    ApiBizError.AUTH_BAD_CREDENTIALS.getCode(),
                    ApiBizError.AUTH_BAD_CREDENTIALS.getMessage());
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), "user");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());
        data.put("user", userData);

        return Result.success("Login successful", data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        return Result.success("Logout successful", null);
    }
}
