package com.example.gamecenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gamecenter.dto.AvatarUploadResult;
import com.example.gamecenter.entity.User;
import org.springframework.web.multipart.MultipartFile;

/** 用户注册、资料、密码与头像业务。 */
public interface UserService extends IService<User> {
    User findByUsername(String username);

    User findByEmail(String email);

    boolean register(User user);

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    boolean changePasswordByEmailCode(Long userId, String verificationCode, String newPassword);
    User updateProfile(Long userId, String displayName, String avatarUrl);
    AvatarUploadResult uploadAvatar(Long userId, MultipartFile file);
}
