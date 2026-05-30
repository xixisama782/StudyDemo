package com.example.gamecenter.utils;

import com.example.gamecenter.constant.ApiConstants;

/** 头像 URL 展示层工具（默认图回退，不改库内值）。 */
public final class AvatarUrlUtils {

    private AvatarUrlUtils() {
    }

    /**
     * API 返回用：数据库为空时统一回退到默认静态头像 URL，不改变库内存储。
     */
    public static String resolveForResponse(String storedAvatarUrl) {
        if (storedAvatarUrl == null || storedAvatarUrl.isBlank()) {
            return ApiConstants.Users.DEFAULT_AVATAR_URL;
        }
        return storedAvatarUrl;
    }
}
