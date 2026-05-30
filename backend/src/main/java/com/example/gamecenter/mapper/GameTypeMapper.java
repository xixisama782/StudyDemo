package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.GameType;
import org.apache.ibatis.annotations.Mapper;

/** game_types 表 MyBatis 基础 Mapper。 */
@Mapper
public interface GameTypeMapper extends BaseMapper<GameType> {
}
