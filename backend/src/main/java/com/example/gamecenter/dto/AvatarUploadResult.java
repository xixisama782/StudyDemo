package com.example.gamecenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 头像上传成功后的 API 响应字段。 */
@Data
@AllArgsConstructor
public class AvatarUploadResult {
    private String avatarUrl;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private long size;
}
