package com.example.gamecenter.service;

import com.example.gamecenter.entity.Game;
import com.example.gamecenter.utils.Result;

/** 游戏列表、详情与管理端 CRUD。 */
public interface GameService {
    Result<Object> getGameList(Long typeId, String keyword, Integer page, Integer pageSize);
    Result<Object> getGameById(Long id);
    Result<Object> getAdminGameList(Long typeId, String keyword, Integer page, Integer pageSize);
    Result<Object> createGame(Game game);
    Result<Object> updateGame(Long id, Game game);
    Result<Object> deleteGame(Long id);
}
