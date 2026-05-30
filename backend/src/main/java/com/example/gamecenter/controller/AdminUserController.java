package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.AdminUserService;
import com.example.gamecenter.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 管理端用户列表、详情、状态与密码重置。 */
@RestController
@RequestMapping(ApiConstants.AdminUsers.BASE)
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public Result<Object> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        if (request.getAttribute("adminId") == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return adminUserService.getUserList(keyword, status, page, pageSize);
    }

    @GetMapping("/{id}")
    public Result<Object> getUserById(@PathVariable Long id, HttpServletRequest request) {
        if (request.getAttribute("adminId") == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return adminUserService.getUserById(id);
    }

    @PutMapping("/{id}/status")
    public Result<Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        if (request.getAttribute("adminId") == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        String status = body != null ? body.get("status") : null;
        return adminUserService.updateUserStatus(id, status);
    }

    @PutMapping("/{id}/password")
    public Result<Object> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        if (request.getAttribute("adminId") == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        String newPassword = body != null ? body.get("newPassword") : null;
        return adminUserService.resetUserPassword(id, newPassword);
    }
}
