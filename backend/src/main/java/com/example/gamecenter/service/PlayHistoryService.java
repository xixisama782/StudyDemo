package com.example.gamecenter.service;

import com.example.gamecenter.utils.Result;

/** 用户游玩历史与游戏 play_count 计数。 */
public interface PlayHistoryService {
    Result<Object> getHistory(Long userId, Long gameId, Integer page, Integer pageSize);
    Result<Object> recordHistory(Long userId, Long gameId, Integer durationSeconds, Long score, String meta);
    Result<Object> recordPlay(Long userId, Long gameId, Integer durationSeconds, Long score);
}
