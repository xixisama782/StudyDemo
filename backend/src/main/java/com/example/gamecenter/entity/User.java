package com.example.gamecenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 注册用户实体，对应 users 表。 */
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希，非明文 */
    private String passwordHash;

    private String email;

    private String displayName;

    private String avatarUrl;

    /** 账户状态：normal / disabled */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}