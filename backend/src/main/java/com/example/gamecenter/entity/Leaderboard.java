package com.example.gamecenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 游戏排行榜记录，对应 leaderboards 表。 */
@Data
@TableName("leaderboards")
public class Leaderboard {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long gameId;

    private Long userId;

    private Long score;

    private Integer rankPosition;

    /** 榜单类型，如 all_time */
    private String type;

    /** 周期榜起止（全时段榜可为空） */
    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
