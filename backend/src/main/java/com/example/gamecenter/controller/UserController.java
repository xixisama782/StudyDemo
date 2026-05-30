package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.constant.EmailVerificationPurpose;
import com.example.gamecenter.dto.AvatarUploadResult;
import com.example.gamecenter.entity.User;
import com.example.gamecenter.exception.BusinessException;
import com.example.gamecenter.service.EmailVerificationService;
import com.example.gamecenter.service.UserService;
import com.example.gamecenter.utils.AvatarUrlUtils;
import com.example.gamecenter.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/** 当前登录用户资料、头像上传与密码修改（验证码或原密码二选一）。 */
@RestController
@RequestMapping(ApiConstants.Users.BASE)
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    public UserController(UserService userService, EmailVerificationService emailVerificationService) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ApiBizError.USER_NOT_FOUND.getCode(), ApiBizError.USER_NOT_FOUND.getMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("displayName", user.getDisplayName());
        data.put("avatarUrl", AvatarUrlUtils.resolveForResponse(user.getAvatarUrl()));
        data.put("status", user.getStatus());
        data.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        return Result.success(data);
    }

    @PutMapping("/me")
    public Result<Map<String, Object>> updateProfile(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        String displayName = request.get("displayName");
        String avatarUrl = request.get("avatarUrl");

        User user = userService.updateProfile(userId, displayName, avatarUrl);
        if (user == null) {
            return Result.error(ApiBizError.USER_NOT_FOUND.getCode(), ApiBizError.USER_NOT_FOUND.getMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("displayName", user.getDisplayName());
        data.put("avatarUrl", AvatarUrlUtils.resolveForResponse(user.getAvatarUrl()));

        return Result.success("Profile updated successfully", data);
    }

    @PostMapping(ApiConstants.Users.ME_PASSWORD_SEND_CODE)
    public Result<Void> sendPasswordChangeCode(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ApiBizError.USER_NOT_FOUND.getCode(), ApiBizError.USER_NOT_FOUND.getMessage());
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return Result.error(
                    ApiBizError.USER_EMAIL_NOT_BOUND.getCode(),
                    ApiBizError.USER_EMAIL_NOT_BOUND.getMessage());
        }

        try {
            emailVerificationService.sendCode(user.getEmail(), EmailVerificationPurpose.CHANGE_PASSWORD);
            return Result.success("验证码已发送至绑定邮箱", null);
        } catch (BusinessException ex) {
            return Result.error(ex.getCode(), ex.getMessage());
        }
    }

    @PostMapping(ApiConstants.Users.ME_AVATAR)
    public Result<AvatarUploadResult> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        AvatarUploadResult result = userService.uploadAvatar(userId, file);
        return Result.success("Avatar uploaded successfully", result);
    }

    /** 优先邮箱验证码改密；否则校验原密码。 */
    @PutMapping("/me/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        String verificationCode = request.get("verificationCode");

        if (newPassword == null || newPassword.isBlank()) {
            return Result.error(
                    ApiBizError.USER_PASSWORD_BOTH_REQUIRED.getCode(),
                    ApiBizError.USER_PASSWORD_BOTH_REQUIRED.getMessage());
        }
        if (newPassword.length() < 6) {
            return Result.error(
                    ApiBizError.ADMIN_NEW_PASSWORD_TOO_SHORT.getCode(),
                    ApiBizError.ADMIN_NEW_PASSWORD_TOO_SHORT.getMessage());
        }

        if (verificationCode != null && !verificationCode.isBlank()) {
            if (userService.changePasswordByEmailCode(userId, verificationCode, newPassword)) {
                return Result.success("Password updated successfully", null);
            }
            return Result.error(
                    ApiBizError.AUTH_VERIFICATION_CODE_INVALID.getCode(),
                    ApiBizError.AUTH_VERIFICATION_CODE_INVALID.getMessage());
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            return Result.error(
                    ApiBizError.USER_PASSWORD_CODE_OR_OLD_REQUIRED.getCode(),
                    ApiBizError.USER_PASSWORD_CODE_OR_OLD_REQUIRED.getMessage());
        }

        if (userService.changePassword(userId, oldPassword, newPassword)) {
            return Result.success("Password updated successfully", null);
        }
        return Result.error(
                ApiBizError.USER_OLD_PASSWORD_WRONG.getCode(),
                ApiBizError.USER_OLD_PASSWORD_WRONG.getMessage());
    }
}
