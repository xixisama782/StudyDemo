package com.example.gamecenter.service.impl;

import com.example.gamecenter.mapper.FavoriteMapper;
import com.example.gamecenter.mapper.LeaderboardMapper;
import com.example.gamecenter.mapper.PlayHistoryMapper;
import com.example.gamecenter.service.UserStatsService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/** 聚合游玩次数、时长、收藏数及历史最佳排名。 */
@Service
public class UserStatsServiceImpl implements UserStatsService {

    @Autowired
    private PlayHistoryMapper playHistoryMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private LeaderboardMapper leaderboardMapper;

    @Override
    public Result<Object> getUserStatistics(Long userId) {
        Long totalPlayCount = playHistoryMapper.countByUserId(userId);
        if (totalPlayCount == null) {
            totalPlayCount = 0L;
        }

        Long totalDurationSeconds = playHistoryMapper.sumDurationSecondsByUserId(userId);
        if (totalDurationSeconds == null) {
            totalDurationSeconds = 0L;
        }

        Long favoriteCount = favoriteMapper.countByUserId(userId);
        if (favoriteCount == null) {
            favoriteCount = 0L;
        }

        int bestRank = 0;
        Long bestRankGameId = null;
        String bestRankGameName = null;

        List<Map<String, Object>> allRanks = leaderboardMapper.selectUserAllRanks(userId);
        if (allRanks != null && !allRanks.isEmpty()) {
            for (Map<String, Object> rank : allRanks) {
                Integer rankPosition = rank.get("rankPosition") != null ? 
                    Integer.valueOf(rank.get("rankPosition").toString()) : null;
                if (rankPosition != null && (bestRank == 0 || rankPosition < bestRank)) {
                    bestRank = rankPosition;
                    bestRankGameId = rank.get("gameId") != null ? 
                        Long.valueOf(rank.get("gameId").toString()) : null;
                    bestRankGameName = rank.get("gameName") != null ? 
                        rank.get("gameName").toString() : null;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalPlayCount", totalPlayCount);
        result.put("totalDurationSeconds", totalDurationSeconds);
        result.put("favoriteCount", favoriteCount);
        result.put("bestRank", bestRank > 0 ? bestRank : null);
        result.put("bestRankGameId", bestRankGameId);
        result.put("bestRankGameName", bestRankGameName);

        return Result.success(result);
    }

    @Override
    public Result<Object> getUserLeaderboards(Long userId) {
        List<Map<String, Object>> allRanks = leaderboardMapper.selectUserAllRanks(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        if (allRanks != null) {
            for (Map<String, Object> rank : allRanks) {
                Map<String, Object> item = new HashMap<>();
                item.put("gameId", rank.get("gameId"));
                item.put("gameName", rank.get("gameName"));
                item.put("thumbnailUrl", rank.get("thumbnailUrl"));
                item.put("rankPosition", rank.get("rankPosition"));
                item.put("score", rank.get("score"));
                item.put("createdAt", rank.get("createdAt"));
                result.add(item);
            }
        }

        return Result.success(result);
    }
}
