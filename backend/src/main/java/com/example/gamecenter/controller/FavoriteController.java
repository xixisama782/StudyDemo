package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.FavoriteService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 用户收藏游戏的增删查与批量校验。 */
@RestController
@RequestMapping(ApiConstants.Favorites.BASE)
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public Result<Object> getFavorites(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return favoriteService.getFavorites(userId, page, pageSize);
    }

    @PostMapping(ApiConstants.Favorites.BATCH_CHECK)
    public Result<Object> checkFavoritesBatch(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }
        @SuppressWarnings("unchecked")
        List<Object> raw = body != null ? (List<Object>) body.get("gameIds") : null;
        List<Long> gameIds = new ArrayList<>();
        if (raw != null) {
            for (Object o : raw) {
                if (o != null) {
                    gameIds.add(Long.valueOf(o.toString()));
                }
            }
        }
        return favoriteService.checkFavoritesBatch(userId, gameIds);
    }

    @PostMapping
    public Result<Object> addFavorite(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        Long gameId = body.get("gameId");
        if (gameId == null) {
            return Result.error(
                    ApiBizError.PARAM_GAME_ID_REQUIRED.getCode(),
                    ApiBizError.PARAM_GAME_ID_REQUIRED.getMessage());
        }

        return favoriteService.addFavorite(userId, gameId);
    }

    @DeleteMapping("/{gameId}")
    public Result<Object> removeFavorite(@PathVariable Long gameId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return favoriteService.removeFavorite(userId, gameId);
    }

    @GetMapping("/{gameId}/check")
    public Result<Object> checkFavorite(@PathVariable Long gameId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return favoriteService.checkFavorite(userId, gameId);
    }
}
