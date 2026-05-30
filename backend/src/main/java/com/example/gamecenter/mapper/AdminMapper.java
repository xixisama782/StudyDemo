package com.example.gamecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gamecenter.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/** admins 表 MyBatis 基础 Mapper。 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}