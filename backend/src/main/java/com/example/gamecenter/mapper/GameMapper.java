package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** games 表：公开/管理端列表、详情与类型计数。 */
@Mapper
public interface GameMapper extends BaseMapper<Game> {

    @Select("<script>" +
            "SELECT g.id, g.name, g.description, g.type_id as typeId, gt.name as typeName, " +
            "g.thumbnail_url as thumbnailUrl, g.play_count as playCount, g.is_active as isActive " +
            "FROM games g " +
            "LEFT JOIN game_types gt ON g.type_id = gt.id " +
            "WHERE g.is_active = 1 " +
            "<if test='typeId != null'>AND g.type_id = #{typeId}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND g.name LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "ORDER BY g.id ASC " +
            "<if test='offset != null and limit != null'>LIMIT #{offset}, #{limit}</if>" +
            "</script>")
    List<Map<String, Object>> selectGameList(@Param("typeId") Long typeId,
                                               @Param("keyword") String keyword,
                                               @Param("offset") Integer offset,
                                               @Param("limit") Integer limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM games g WHERE g.is_active = 1 " +
            "<if test='typeId != null'>AND g.type_id = #{typeId}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND g.name LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "</script>")
    Long selectGameCount(@Param("typeId") Long typeId, @Param("keyword") String keyword);

    @Select("SELECT g.id, g.name, g.description, g.type_id as typeId, gt.name as typeName, " +
            "g.resource_url as resourceUrl, g.thumbnail_url as thumbnailUrl, " +
            "g.provider, g.tags, g.is_active as isActive, g.play_count as playCount, " +
            "g.last_played_at as lastPlayedAt, g.created_at as createdAt " +
            "FROM games g " +
            "LEFT JOIN game_types gt ON g.type_id = gt.id " +
            "WHERE g.id = #{id}")
    Map<String, Object> selectGameById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM games WHERE type_id = #{typeId}")
    Long countByTypeId(@Param("typeId") Long typeId);

    @Select("<script>" +
            "SELECT g.id, g.name, g.description, g.type_id as typeId, gt.name as typeName, " +
            "g.thumbnail_url as thumbnailUrl, g.play_count as playCount, g.is_active as isActive " +
            "FROM games g " +
            "LEFT JOIN game_types gt ON g.type_id = gt.id " +
            "WHERE 1=1 " +
            "<if test='typeId != null'>AND g.type_id = #{typeId}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND g.name LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "ORDER BY g.id ASC " +
            "<if test='offset != null and limit != null'>LIMIT #{offset}, #{limit}</if>" +
            "</script>")
    List<Map<String, Object>> selectAdminGameList(@Param("typeId") Long typeId,
                                                  @Param("keyword") String keyword,
                                                  @Param("offset") Integer offset,
                                                  @Param("limit") Integer limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM games g WHERE 1=1 " +
            "<if test='typeId != null'>AND g.type_id = #{typeId}</if> " +
            "<if test='keyword != null and keyword != \"\"'>AND g.name LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "</script>")
    Long selectAdminGameCount(@Param("typeId") Long typeId, @Param("keyword") String keyword);

    @Select("SELECT play_count FROM games WHERE id = #{id}")
    Long getPlayCountById(@Param("id") Long id);
}
