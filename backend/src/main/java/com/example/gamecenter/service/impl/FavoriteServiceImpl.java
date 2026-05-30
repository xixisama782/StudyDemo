package com.example.gamecenter.service.impl;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.entity.Favorite;
import com.example.gamecenter.mapper.FavoriteMapper;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.service.FavoriteService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** 收藏增删查与批量校验已收藏 gameId。 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private GameMapper gameMapper;

    @Override
    public Result<Object> getFavorites(Long userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Integer offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = favoriteMapper.selectFavoritesWithGameInfo(userId, offset, pageSize);
        Long total = favoriteMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("list", list);

        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> addFavorite(Long userId, Long gameId) {
        Favorite existing = favoriteMapper.selectByUserIdAndGameId(userId, gameId);
        if (existing != null) {
            return Result.error(
                    ApiBizError.FAVORITE_ALREADY_EXISTS.getCode(),
                    ApiBizError.FAVORITE_ALREADY_EXISTS.getMessage());
        }

        Map<String, Object> game = gameMapper.selectGameById(gameId);
        if (game == null) {
            return Result.error(ApiBizError.GAME_NOT_FOUND.getCode(), ApiBizError.GAME_NOT_FOUND.getMessage());
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGameId(gameId);
        favorite.setCreatedAt(LocalDateTime.now());

        favoriteMapper.insert(favorite);

        Map<String, Object> data = new HashMap<>();
        data.put("id", favorite.getId());
        data.put("gameId", gameId);

        return Result.success("Game added to favorites", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> removeFavorite(Long userId, Long gameId) {
        Favorite favorite = favoriteMapper.selectByUserIdAndGameId(userId, gameId);
        if (favorite == null) {
            return Result.error(
                    ApiBizError.FAVORITE_NOT_FOUND.getCode(),
                    ApiBizError.FAVORITE_NOT_FOUND.getMessage());
        }

        favoriteMapper.deleteById(favorite.getId());
        return Result.success("Game removed from favorites", null);
    }

    @Override
    public Result<Object> checkFavorite(Long userId, Long gameId) {
        Favorite favorite = favoriteMapper.selectByUserIdAndGameId(userId, gameId);

        Map<String, Object> data = new HashMap<>();
        data.put("isFavorited", favorite != null);

        return Result.success(data);
    }

    @Override
    public Result<Object> checkFavoritesBatch(Long userId, List<Long> gameIds) {
        Map<String, Object> data = new HashMap<>();
        if (gameIds == null || gameIds.isEmpty()) {
            data.put("favoritedGameIds", new ArrayList<Long>());
            return Result.success(data);
        }
        LinkedHashSet<Long> unique = new LinkedHashSet<>();
        for (Long id : gameIds) {
            if (id != null && id > 0) {
                unique.add(id);
            }
        }
        List<Long> distinct = new ArrayList<>(unique);
        if (distinct.isEmpty()) {
            data.put("favoritedGameIds", new ArrayList<Long>());
            return Result.success(data);
        }
        List<Long> favorited = favoriteMapper.selectFavoritedGameIds(userId, distinct);
        data.put("favoritedGameIds", favorited != null ? favorited : new ArrayList<Long>());
        return Result.success(data);
    }
}
