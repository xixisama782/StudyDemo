package com.example.gamecenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 游戏分类实体，对应 game_types 表。 */
@Data
@TableName("game_types")
public class GameType {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 唯一业务代码，用于前端/路由识别 */
    private String code;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
