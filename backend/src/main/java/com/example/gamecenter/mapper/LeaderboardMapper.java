package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.Leaderboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** leaderboards 表：榜单、个人排名与计分统计。 */
@Mapper
public interface LeaderboardMapper extends BaseMapper<Leaderboard> {

    List<Map<String, Object>> selectLeaderboard(@Param("gameId") Long gameId,
                                               @Param("type") String type,
                                               @Param("limit") Integer limit);

    @Select("SELECT * FROM leaderboards " +
            "WHERE game_id = #{gameId} AND user_id = #{userId} AND type = #{type}")
    Leaderboard selectByGameUserType(@Param("gameId") Long gameId,
                                    @Param("userId") Long userId,
                                    @Param("type") String type);

    Map<String, Object> selectUserRank(@Param("gameId") Long gameId,
                                       @Param("userId") Long userId,
                                       @Param("type") String type);

    @Select("SELECT COUNT(*) FROM leaderboards " +
            "WHERE game_id = #{gameId} AND type = #{type} AND score > #{score}")
    Long countHigherScores(@Param("gameId") Long gameId,
                          @Param("type") String type,
                          @Param("score") Long score);

    List<Map<String, Object>> selectUserAllRanks(@Param("userId") Long userId);
}
