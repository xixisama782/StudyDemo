package com.example.gamecenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 单次游玩会话，对应 game_sessions 表。 */
@Data
@TableName("game_sessions")
public class GameSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long gameId;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Integer durationSeconds;

    private Long score;

    /** 扩展 JSON 或业务附加信息 */
    private String meta;

    /** active / ended / abandoned */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
