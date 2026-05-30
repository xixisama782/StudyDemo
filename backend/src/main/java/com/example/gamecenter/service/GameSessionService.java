package com.example.gamecenter.service;

import com.example.gamecenter.utils.Result;

/** 游戏会话生命周期：开始、结束与查询进行中会话。 */
public interface GameSessionService {
    Result<Object> startSession(Long userId, Long gameId);
    Result<Object> endSession(Long userId, Long sessionId, Long score, String meta);
    Result<Object> getCurrentSession(Long userId);
}
