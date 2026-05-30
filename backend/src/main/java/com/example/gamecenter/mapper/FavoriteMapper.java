package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** favorites 表及联表查询。 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT f.id, f.game_id as gameId, g.name as gameName, " +
            "g.thumbnail_url as thumbnailUrl, gt.name as typeName, f.created_at as createdAt " +
            "FROM favorites f " +
            "JOIN games g ON f.game_id = g.id " +
            "LEFT JOIN game_types gt ON g.type_id = gt.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectFavoritesWithGameInfo(@Param("userId") Long userId,
                                                           @Param("offset") Integer offset,
                                                           @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM favorites WHERE user_id = #{userId} AND game_id = #{gameId}")
    Favorite selectByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);

    /**
     * 在给定 gameIds 中返回当前用户已收藏的 game_id（用于批量消除 N+1）。
     */
    List<Long> selectFavoritedGameIds(@Param("userId") Long userId, @Param("gameIds") List<Long> gameIds);
}
