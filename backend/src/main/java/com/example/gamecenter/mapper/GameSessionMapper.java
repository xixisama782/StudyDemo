package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.GameSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/** game_sessions 表：进行中会话与废弃 active 会话。 */
@Mapper
public interface GameSessionMapper extends BaseMapper<GameSession> {

    @Select("SELECT s.id as sessionId, s.game_id as gameId, g.name as gameName, " +
            "s.started_at as startedAt " +
            "FROM game_sessions s " +
            "JOIN games g ON s.game_id = g.id " +
            "WHERE s.user_id = #{userId} AND s.status = 'active' " +
            "ORDER BY s.started_at DESC LIMIT 1")
    Map<String, Object> selectActiveSession(@Param("userId") Long userId);

    @Update("UPDATE game_sessions SET status = 'abandoned', ended_at = NOW(), " +
            "duration_seconds = TIMESTAMPDIFF(SECOND, started_at, NOW()) " +
            "WHERE user_id = #{userId} AND status = 'active'")
    int abandonActiveSessions(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM game_sessions WHERE user_id = #{userId} AND status = 'ended'")
    Long countEndedSessionsByUserId(@Param("userId") Long userId);
}
