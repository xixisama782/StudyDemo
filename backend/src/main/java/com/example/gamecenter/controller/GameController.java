package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.GameService;
import com.example.gamecenter.service.PlayHistoryService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/** 公开游戏列表/详情、管理端游戏列表与游玩计数。 */
@RestController
@RequestMapping(ApiConstants.Games.BASE)
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayHistoryService playHistoryService;

    @GetMapping
    public Result<Object> getGameList(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return gameService.getGameList(typeId, keyword, page, pageSize);
    }

    @GetMapping("/{id}")
    public Result<Object> getGameById(@PathVariable Long id) {
        return gameService.getGameById(id);
    }

    @GetMapping(ApiConstants.Games.ADMIN_LIST)
    public Result<Object> getAdminGameList(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        if (request.getAttribute("adminId") == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return gameService.getAdminGameList(typeId, keyword, page, pageSize);
    }

    /**
     * 记录游戏游玩（更新游玩次数等），见 ApiSpecification 接口 29。
     */
    @PostMapping(ApiConstants.Games.PLAY)
    public Result<Object> recordPlay(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        Integer durationSeconds = body != null && body.get("durationSeconds") != null
                ? Integer.valueOf(body.get("durationSeconds").toString())
                : null;
        Long score = body != null && body.get("score") != null
                ? Long.valueOf(body.get("score").toString())
                : null;

        return playHistoryService.recordPlay(userId, id, durationSeconds, score);
    }
}
