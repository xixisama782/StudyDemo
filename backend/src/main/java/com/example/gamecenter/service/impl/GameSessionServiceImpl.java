package com.example.gamecenter.service.impl;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.entity.Game;
import com.example.gamecenter.entity.GameSession;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.mapper.GameSessionMapper;
import com.example.gamecenter.mapper.PlayHistoryMapper;
import com.example.gamecenter.service.GameSessionService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/** 游戏会话：新开前废弃旧 active 会话，结束时幂等并同步游玩历史。 */
@Service
public class GameSessionServiceImpl implements GameSessionService {

    @Autowired
    private GameSessionMapper gameSessionMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private PlayHistoryMapper playHistoryMapper;

    @Override
    @Transactional
    public Result<Object> startSession(Long userId, Long gameId) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            return Result.error(ApiBizError.GAME_NOT_FOUND.getCode(), ApiBizError.GAME_NOT_FOUND.getMessage());
        }

        // 同一用户同时仅保留一个 active 会话
        gameSessionMapper.abandonActiveSessions(userId);

        GameSession session = new GameSession();
        session.setUserId(userId);
        session.setGameId(gameId);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus("active");
        session.setDurationSeconds(0);
        session.setScore(0L);

        gameSessionMapper.insert(session);

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", session.getId());
        data.put("gameId", gameId);
        data.put("gameName", game.getName());
        data.put("startedAt", session.getStartedAt().toString());

        return Result.success("Game session started", data);
    }

    @Override
    @Transactional
    public Result<Object> endSession(Long userId, Long sessionId, Long score, String meta) {
        GameSession session = gameSessionMapper.selectById(sessionId);

        if (session == null) {
            return Result.error(
                    ApiBizError.SESSION_NOT_FOUND.getCode(),
                    ApiBizError.SESSION_NOT_FOUND.getMessage());
        }

        if (!session.getUserId().equals(userId)) {
            return Result.error(
                    ApiBizError.SESSION_ACCESS_DENIED.getCode(),
                    ApiBizError.SESSION_ACCESS_DENIED.getMessage());
        }

        if (!"active".equals(session.getStatus())) {
            if ("ended".equals(session.getStatus())) {
                // 重复结束请求返回 alreadyEnded，供 beacon 等场景幂等
                Map<String, Object> idempotent = new HashMap<>();
                idempotent.put("sessionId", session.getId());
                idempotent.put("gameId", session.getGameId());
                idempotent.put("startedAt", session.getStartedAt().toString());
                idempotent.put("endedAt", session.getEndedAt() != null ? session.getEndedAt().toString() : null);
                idempotent.put("durationSeconds", session.getDurationSeconds() != null ? session.getDurationSeconds() : 0);
                idempotent.put("alreadyEnded", true);
                return Result.success("Game session already ended", idempotent);
            }
            return Result.error(
                    ApiBizError.SESSION_ALREADY_ENDED.getCode(),
                    ApiBizError.SESSION_ALREADY_ENDED.getMessage());
        }

        LocalDateTime endedAt = LocalDateTime.now();
        int durationSeconds = (int) ChronoUnit.SECONDS.between(session.getStartedAt(), endedAt);

        session.setEndedAt(endedAt);
        session.setDurationSeconds(durationSeconds);
        session.setScore(score != null ? score : 0L);
        session.setMeta(meta);
        session.setStatus("ended");

        gameSessionMapper.updateById(session);

        try {
            playHistoryMapper.upsertMergePlayHistory(
                    userId,
                    session.getGameId(),
                    endedAt,
                    durationSeconds,
                    score != null ? score : 0L,
                    meta
            );
        } catch (Exception e) {
            System.err.println("同步游玩历史失败: " + e.getMessage());
            e.printStackTrace();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", session.getId());
        data.put("gameId", session.getGameId());
        data.put("startedAt", session.getStartedAt().toString());
        data.put("endedAt", endedAt.toString());
        data.put("durationSeconds", durationSeconds);

        return Result.success("Game session ended", data);
    }

    @Override
    public Result<Object> getCurrentSession(Long userId) {
        Map<String, Object> session = gameSessionMapper.selectActiveSession(userId);

        if (session == null) {
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("sessionId", null);
            return Result.success(emptyData);
        }

        return Result.success(session);
    }
}
