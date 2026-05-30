package com.example.gamecenter.service.impl;

import com.example.gamecenter.entity.Leaderboard;
import com.example.gamecenter.mapper.LeaderboardMapper;
import com.example.gamecenter.service.LeaderboardService;
import com.example.gamecenter.utils.AvatarUrlUtils;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 排行榜：仅当新分数更高时更新，并按更高分数量计算名次。 */
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    @Autowired
    private LeaderboardMapper leaderboardMapper;

    @Override
    public Result<Object> getLeaderboard(Long gameId, String type, Integer limit) {
        if (type == null || type.isEmpty()) {
            type = "all_time";
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }

        List<Map<String, Object>> list = leaderboardMapper.selectLeaderboard(gameId, type, limit);
        // MyBatis 可能返回 avatarurl 小写键，统一为 avatarUrl 并回退默认头像
        for (Map<String, Object> row : list) {
            Object av = row.get("avatarUrl");
            if (av == null) {
                av = row.get("avatarurl");
            }
            String s = av == null ? null : av.toString();
            row.put("avatarUrl", AvatarUrlUtils.resolveForResponse(s));
        }
        return Result.success(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> submitScore(Long userId, Long gameId, Long score, String type) {
        if (type == null || type.isEmpty()) {
            type = "all_time";
        }
        long submitted = score != null ? score : 0L;

        Leaderboard existing = leaderboardMapper.selectByGameUserType(gameId, userId, type);
        boolean isNewRecord = false;

        if (existing == null) {
            Leaderboard leaderboard = new Leaderboard();
            leaderboard.setGameId(gameId);
            leaderboard.setUserId(userId);
            leaderboard.setScore(submitted);
            leaderboard.setType(type);
            leaderboard.setCreatedAt(LocalDateTime.now());
            leaderboardMapper.insert(leaderboard);
            isNewRecord = true;
        } else if (submitted > existing.getScore()) {
            existing.setScore(submitted);
            existing.setUpdatedAt(LocalDateTime.now());
            leaderboardMapper.updateById(existing);
            isNewRecord = true;
        }

        Leaderboard row = leaderboardMapper.selectByGameUserType(gameId, userId, type);
        if (row == null) {
            return Result.error(500, "Leaderboard sync failed");
        }

        long effectiveScore = row.getScore();
        Long higherCount = leaderboardMapper.countHigherScores(gameId, type, effectiveScore);
        int rank = higherCount != null ? higherCount.intValue() + 1 : 1;

        Map<String, Object> data = new HashMap<>();
        data.put("score", effectiveScore);
        data.put("rankPosition", rank);
        data.put("isNewRecord", isNewRecord);

        return Result.success("Score submitted successfully", data);
    }

    @Override
    public Result<Object> getUserRank(Long userId, Long gameId, String type) {
        if (type == null || type.isEmpty()) {
            type = "all_time";
        }

        Map<String, Object> rankData = leaderboardMapper.selectUserRank(gameId, userId, type);

        if (rankData == null) {
            return Result.success(null);
        }

        return Result.success(rankData);
    }
}
