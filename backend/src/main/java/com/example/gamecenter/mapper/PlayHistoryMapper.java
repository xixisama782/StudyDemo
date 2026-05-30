package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.PlayHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** play_history 表：聚合 upsert 与统计查询。 */
@Mapper
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {

    /**
     * 同一用户同一游戏仅一行：冲突时累加 duration_seconds，覆盖 played_at / score / meta。
     */
    int upsertMergePlayHistory(@Param("userId") Long userId,
                               @Param("gameId") Long gameId,
                               @Param("playedAt") LocalDateTime playedAt,
                               @Param("durationSeconds") Integer durationSeconds,
                               @Param("score") Long score,
                               @Param("meta") String meta);

    List<Map<String, Object>> selectHistoryWithGameInfo(@Param("userId") Long userId,
                                                      @Param("gameId") Long gameId,
                                                      @Param("offset") Integer offset,
                                                      @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM play_history WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(duration_seconds), 0) FROM play_history WHERE user_id = #{userId}")
    Long sumDurationSecondsByUserId(@Param("userId") Long userId);
}
