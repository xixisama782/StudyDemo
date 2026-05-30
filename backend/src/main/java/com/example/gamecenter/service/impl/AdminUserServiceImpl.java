package com.example.gamecenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.entity.User;
import com.example.gamecenter.mapper.FavoriteMapper;
import com.example.gamecenter.mapper.GameSessionMapper;
import com.example.gamecenter.mapper.UserMapper;
import com.example.gamecenter.service.AdminUserService;
import com.example.gamecenter.utils.AvatarUrlUtils;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 管理端用户维护：列表筛选、状态变更与 BCrypt 重置密码。 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final GameSessionMapper gameSessionMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AdminUserServiceImpl(UserMapper userMapper, FavoriteMapper favoriteMapper,
                                GameSessionMapper gameSessionMapper) {
        this.userMapper = userMapper;
        this.favoriteMapper = favoriteMapper;
        this.gameSessionMapper = gameSessionMapper;
    }

    @Override
    public Result<Object> getUserList(String keyword, String status, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        QueryWrapper<User> w = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            w.and(q -> q.like("username", kw).or().like("email", kw));
        }
        if (StringUtils.hasText(status)) {
            w.eq("status", status.trim());
        }
        w.orderByDesc("id");

        Long total = userMapper.selectCount(w);
        int offset = (page - 1) * pageSize;
        w.last("LIMIT " + offset + ", " + pageSize);
        List<User> records = userMapper.selectList(w);

        List<Map<String, Object>> list = records.stream()
                .map(this::toAdminListItem)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("total", total == null ? 0L : total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("list", list);

        return Result.success(data);
    }

    @Override
    public Result<Object> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ApiBizError.USER_NOT_FOUND.getCode(), ApiBizError.USER_NOT_FOUND.getMessage());
        }

        Long playCount = gameSessionMapper.countEndedSessionsByUserId(id);
        if (playCount == null) {
            playCount = 0L;
        }
        Long favoriteCount = favoriteMapper.countByUserId(id);
        if (favoriteCount == null) {
            favoriteCount = 0L;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("displayName", user.getDisplayName());
        data.put("avatarUrl", AvatarUrlUtils.resolveForResponse(user.getAvatarUrl()));
        data.put("status", user.getStatus());
        data.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        data.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        data.put("playCount", playCount);
        data.put("favoriteCount", favoriteCount);

        return Result.success(data);
    }

    @Override
    @Transactional
    public Result<Object> updateUserStatus(Long id, String status) {
        if (!StringUtils.hasText(status)) {
            return Result.error(
                    ApiBizError.ADMIN_STATUS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_STATUS_REQUIRED.getMessage());
        }
        if (!"normal".equals(status) && !"disabled".equals(status)) {
            return Result.error(
                    ApiBizError.ADMIN_STATUS_INVALID.getCode(),
                    ApiBizError.ADMIN_STATUS_INVALID.getMessage());
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ApiBizError.USER_NOT_FOUND.getCode(), ApiBizError.USER_NOT_FOUND.getMessage());
        }
        user.setStatus(status);
        userMapper.updateById(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("status", user.getStatus());
        return Result.success("User status updated successfully", data);
    }

    @Override
    @Transactional
    public Result<Object> resetUserPassword(Long id, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            return Result.error(
                    ApiBizError.ADMIN_NEW_PASSWORD_REQUIRED.getCode(),
                    ApiBizError.ADMIN_NEW_PASSWORD_REQUIRED.getMessage());
        }
        if (newPassword.length() < 6) {
            return Result.error(
                    ApiBizError.ADMIN_NEW_PASSWORD_TOO_SHORT.getCode(),
                    ApiBizError.ADMIN_NEW_PASSWORD_TOO_SHORT.getMessage());
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ApiBizError.USER_NOT_FOUND.getCode(), ApiBizError.USER_NOT_FOUND.getMessage());
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success("User password reset successfully", null);
    }

    private Map<String, Object> toAdminListItem(User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("email", user.getEmail());
        m.put("displayName", user.getDisplayName());
        m.put("avatarUrl", AvatarUrlUtils.resolveForResponse(user.getAvatarUrl()));
        m.put("status", user.getStatus());
        m.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return m;
    }
}
