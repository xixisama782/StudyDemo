package com.example.gamecenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户每游戏聚合游玩记录，对应 play_history 表（每用户每游戏一行）。 */
@Data
@TableName("play_history")
public class PlayHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long gameId;

    private LocalDateTime playedAt;

    private Integer durationSeconds;

    private Long score;

    /** 最近一次游玩的附加信息 */
    private String meta;
}
