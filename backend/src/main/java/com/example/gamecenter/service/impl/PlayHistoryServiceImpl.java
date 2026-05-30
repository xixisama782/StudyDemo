package com.example.gamecenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.gamecenter.entity.PlayHistory;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.mapper.PlayHistoryMapper;
import com.example.gamecenter.service.PlayHistoryService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 游玩历史 upsert 聚合；recordPlay 仅递增游戏游玩次数。 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PlayHistoryServiceImpl implements PlayHistoryService {

    @Autowired
    private PlayHistoryMapper playHistoryMapper;

    @Autowired
    private GameMapper gameMapper;

    @Override
    @Transactional(readOnly = true)
    public Result<Object> getHistory(Long userId, Long gameId, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Integer offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = playHistoryMapper.selectHistoryWithGameInfo(userId, gameId, offset, pageSize);
        Long total = playHistoryMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);

        return Result.success(result);
    }

    @Override
    public Result<Object> recordHistory(Long userId, Long gameId, Integer durationSeconds, Long score, String meta) {
        PlayHistory history = new PlayHistory();
        history.setUserId(userId);
        history.setGameId(gameId);
        history.setPlayedAt(LocalDateTime.now());
        history.setDurationSeconds(durationSeconds != null ? durationSeconds : 0);
        history.setScore(score != null ? score : 0L);
        history.setMeta(meta);

        playHistoryMapper.upsertMergePlayHistory(
                userId,
                gameId,
                history.getPlayedAt(),
                history.getDurationSeconds(),
                history.getScore(),
                history.getMeta()
        );

        LambdaQueryWrapper<PlayHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(PlayHistory::getUserId, userId).eq(PlayHistory::getGameId, gameId);
        PlayHistory row = playHistoryMapper.selectOne(qw);

        Map<String, Object> data = new HashMap<>();
        data.put("id", row != null ? row.getId() : null);
        data.put("gameId", gameId);
        data.put("playedAt", history.getPlayedAt().toString());

        return Result.success("Play history recorded", data);
    }

    @Override
    public Result<Object> recordPlay(Long userId, Long gameId, Integer durationSeconds, Long score) {
        Long currentPlayCount = gameMapper.getPlayCountById(gameId);
        if (currentPlayCount == null) {
            currentPlayCount = 0L;
        }

        LambdaUpdateWrapper<com.example.gamecenter.entity.Game> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(com.example.gamecenter.entity.Game::getId, gameId)
               .set(com.example.gamecenter.entity.Game::getPlayCount, currentPlayCount + 1)
               .set(com.example.gamecenter.entity.Game::getLastPlayedAt, LocalDateTime.now());
        gameMapper.update(null, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("playCount", currentPlayCount + 1);
        data.put("lastPlayedAt", LocalDateTime.now().toString());

        return Result.success("Play recorded successfully", data);
    }
}
