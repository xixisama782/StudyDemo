package com.example.gamecenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gamecenter.entity.Admin;

/** 管理员账号查询。 */
public interface AdminService extends IService<Admin> {
    Admin findByUsername(String username);
}