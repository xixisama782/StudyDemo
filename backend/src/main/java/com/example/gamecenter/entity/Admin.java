package com.example.gamecenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 后台管理员实体，对应 admins 表。 */
@Data
@TableName("admins")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 管理端登录为明文存储/比对（与用户端 BCrypt 不同） */
    private String passwordHash;

    private String email;

    private String displayName;

    private String role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}