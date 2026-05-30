package com.example.gamecenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 游戏元数据实体，对应 games 表。 */
@Data
@TableName("games")
public class Game {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Long typeId;

    /** 游戏资源入口 URL（iframe/跳转等） */
    private String resourceUrl;

    private String thumbnailUrl;

    private String provider;

    private String tags;

    private String controls;

    /** 是否对用户可见 */
    private Boolean isActive;

    /** 累计游玩次数（由 recordPlay 维护） */
    private Long playCount;

    private LocalDateTime lastPlayedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
