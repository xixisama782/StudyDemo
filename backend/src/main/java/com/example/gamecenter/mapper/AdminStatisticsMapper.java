package com.example.gamecenter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** 管理端统计 SQL（用户数、游玩量、活跃用户等）。 */
@Mapper
public interface AdminStatisticsMapper {

    @Select("SELECT COUNT(*) FROM users")
    Long countTotalUsers();

    @Select("SELECT COUNT(*) FROM games")
    Long countTotalGames();

    @Select("SELECT COALESCE(SUM(play_count), 0) FROM games")
    Long sumTotalPlays();

    @Select("SELECT COUNT(DISTINCT user_id) FROM game_sessions WHERE started_at >= CURDATE()")
    Long countActiveUsersToday();

    @Select("SELECT COUNT(DISTINCT user_id) FROM game_sessions WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
    Long countActiveUsersWeek();

    @Select("SELECT COUNT(DISTINCT user_id) FROM game_sessions WHERE started_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)")
    Long countActiveUsersMonth();

    @Select("SELECT g.id AS gameId, g.name AS gameName, g.thumbnail_url AS thumbnailUrl, " +
            "g.play_count AS playCount, g.last_played_at AS lastPlayedAt " +
            "FROM games g ORDER BY g.play_count DESC LIMIT #{limit}")
    List<Map<String, Object>> selectPopularGames(@Param("limit") int limit);
}
