package com.example.gamecenter.service;

import com.example.gamecenter.entity.GameType;
import com.example.gamecenter.utils.Result;

import java.util.List;

/** 游戏类型查询与管理端维护。 */
public interface GameTypeService {
    Result<Object> getAllGameTypes();
    Result<Object> createGameType(GameType gameType);
    Result<Object> updateGameType(Long id, GameType gameType);
    Result<Object> deleteGameType(Long id);
    boolean existsByCode(String code);
}
