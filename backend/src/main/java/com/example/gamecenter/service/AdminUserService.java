package com.example.gamecenter.service;

import com.example.gamecenter.utils.Result;

/** 管理端用户列表、详情、状态与密码重置。 */
public interface AdminUserService {
    Result<Object> getUserList(String keyword, String status, Integer page, Integer pageSize);

    Result<Object> getUserById(Long id);

    Result<Object> updateUserStatus(Long id, String status);

    Result<Object> resetUserPassword(Long id, String newPassword);
}
