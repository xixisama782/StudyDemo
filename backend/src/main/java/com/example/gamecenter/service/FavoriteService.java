package com.example.gamecenter.service;

import com.example.gamecenter.utils.Result;

import java.util.List;

/** 用户游戏收藏。 */
public interface FavoriteService {
    Result<Object> getFavorites(Long userId, Integer page, Integer pageSize);
    Result<Object> addFavorite(Long userId, Long gameId);
    Result<Object> removeFavorite(Long userId, Long gameId);
    Result<Object> checkFavorite(Long userId, Long gameId);

    Result<Object> checkFavoritesBatch(Long userId, List<Long> gameIds);
}
