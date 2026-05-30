package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.entity.Admin;
import com.example.gamecenter.service.AdminService;
import com.example.gamecenter.utils.JwtUtils;
import com.example.gamecenter.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** 管理端登录与登出（密码明文比对，签发 role=admin JWT）。 */
@RestController
@RequestMapping(ApiConstants.AdminAuth.BASE)
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtUtils jwtUtils;

    public AdminAuthController(AdminService adminService, JwtUtils jwtUtils) {
        this.adminService = adminService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return Result.error(
                    ApiBizError.ADMIN_LOGIN_FIELDS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_LOGIN_FIELDS_REQUIRED.getMessage());
        }

        Admin admin = adminService.findByUsername(username);
        if (admin == null || !password.equals(admin.getPasswordHash())) {
            return Result.error(
                    ApiBizError.AUTH_BAD_CREDENTIALS.getCode(),
                    ApiBizError.AUTH_BAD_CREDENTIALS.getMessage());
        }

        String token = jwtUtils.generateToken(admin.getUsername(), admin.getId(), "admin");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("id", admin.getId());
        adminData.put("username", admin.getUsername());
        adminData.put("email", admin.getEmail());
        adminData.put("displayName", admin.getDisplayName());
        adminData.put("role", admin.getRole());
        data.put("admin", adminData);

        return Result.success("Admin login successful", data);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("Admin logout successful", null);
    }
}