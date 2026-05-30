package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.User;
import org.apache.ibatis.annotations.Mapper;

/** users 表 MyBatis 基础 Mapper。 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}