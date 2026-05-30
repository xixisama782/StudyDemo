package com.example.gamecenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.exception.BusinessException;
import com.example.gamecenter.dto.AvatarUploadResult;
import com.example.gamecenter.entity.User;
import com.example.gamecenter.constant.EmailVerificationPurpose;
import com.example.gamecenter.mapper.UserMapper;
import com.example.gamecenter.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

/** 用户业务实现：BCrypt 密码、邮箱验证码改密与头像文件落盘。 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final DateTimeFormatter AVATAR_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Value("${app.upload.avatar-dir:../uploads/avatars}")
    private String avatarDir = "../uploads/avatars";

    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public UserServiceImpl(
            BCryptPasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return getOne(wrapper);
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email.trim().toLowerCase(Locale.ROOT));
        return getOne(wrapper);
    }

    /** 注册时写入 BCrypt 哈希，邮箱统一小写。 */
    @Override
    public boolean register(User user) {
        if (findByUsername(user.getUsername()) != null) {
            return false;
        }
        if (user.getEmail() != null && findByEmail(user.getEmail()) != null) {
            return false;
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setStatus("normal");
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase(Locale.ROOT));
        }
        return save(user);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return false;
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    /** 邮箱验证码改密：校验通过后一次性消费验证码。 */
    @Override
    public boolean changePasswordByEmailCode(Long userId, String verificationCode, String newPassword) {
        User user = getById(userId);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }
        if (!emailVerificationService.verifyAndConsume(
                user.getEmail(), EmailVerificationPurpose.CHANGE_PASSWORD, verificationCode)) {
            return false;
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    public User updateProfile(Long userId, String displayName, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            return null;
        }

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        updateById(user);
        return user;
    }

    /** 写盘成功后更新库内 URL，并清理旧头像文件（跳过默认图）。 */
    @Override
    @Transactional
    public AvatarUploadResult uploadAvatar(Long userId, MultipartFile file) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported avatar file type");
        }

        String extension = resolveExtension(file.getOriginalFilename(), contentType);
        String storedFileName = buildStoredFileName(userId, extension);
        String avatarUrl = "/uploads/avatars/" + storedFileName;
        Path avatarDirectory = getAvatarDirectory();
        Path targetPath = avatarDirectory.resolve(storedFileName).normalize();
        Path writtenPath = null;
        String previousAvatarUrl = user.getAvatarUrl();

        try {
            Files.createDirectories(avatarDirectory);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            writtenPath = targetPath;

            user.setAvatarUrl(avatarUrl);
            if (!updateById(user)) {
                throw new BusinessException(ApiBizError.USER_AVATAR_PROCESS_FAILED);
            }

            deleteOldAvatar(previousAvatarUrl, avatarUrl);

            return new AvatarUploadResult(
                    avatarUrl,
                    file.getOriginalFilename(),
                    storedFileName,
                    contentType,
                    file.getSize()
            );
        } catch (IOException ex) {
            deleteQuietly(writtenPath);
            throw new BusinessException(ApiBizError.USER_AVATAR_PROCESS_FAILED, ex);
        } catch (RuntimeException ex) {
            deleteQuietly(writtenPath);
            throw ex;
        }
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private String resolveExtension(String originalFileName, String contentType) {
        String extension = extractExtension(originalFileName);
        if (extension != null) {
            switch (extension) {
                case "jpg":
                case "jpeg":
                    return "jpg";
                case "png":
                    return "png";
                case "webp":
                    return "webp";
                default:
                    break;
            }
        }

        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null) {
            return null;
        }
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) {
            return null;
        }
        return originalFileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String buildStoredFileName(Long userId, String extension) {
        String timestamp = LocalDateTime.now().format(AVATAR_TIMESTAMP_FORMATTER);
        return "avatar_" + userId + "_" + timestamp + "." + extension;
    }

    private Path getAvatarDirectory() {
        return Paths.get(avatarDir).toAbsolutePath().normalize();
    }

    private void deleteOldAvatar(String previousAvatarUrl, String currentAvatarUrl) {
        if (previousAvatarUrl == null || previousAvatarUrl.isBlank() || previousAvatarUrl.equals(currentAvatarUrl)) {
            return;
        }
        if (ApiConstants.Users.DEFAULT_AVATAR_URL.equals(previousAvatarUrl)) {
            return;
        }
        if (!previousAvatarUrl.startsWith("/uploads/avatars/")) {
            return;
        }

        String oldFileName = previousAvatarUrl.substring("/uploads/avatars/".length());
        if (oldFileName.isBlank()) {
            return;
        }

        deleteQuietly(getAvatarDirectory().resolve(oldFileName).normalize());
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
