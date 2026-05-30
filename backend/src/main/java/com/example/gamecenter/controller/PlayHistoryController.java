package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.PlayHistoryService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/** 用户游玩历史分页查询与手动记录。 */
@RestController
@RequestMapping(ApiConstants.PlayHistory.BASE)
public class PlayHistoryController {

    @Autowired
    private PlayHistoryService playHistoryService;

    @GetMapping
    public Result<Object> getHistory(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return playHistoryService.getHistory(userId, gameId, page, pageSize);
    }

    @PostMapping
    public Result<Object> recordHistory(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        Long gameId = body.get("gameId") != null ? Long.valueOf(body.get("gameId").toString()) : null;
        if (gameId == null) {
            return Result.error(
                    ApiBizError.PARAM_GAME_ID_REQUIRED.getCode(),
                    ApiBizError.PARAM_GAME_ID_REQUIRED.getMessage());
        }

        Integer durationSeconds = body.get("durationSeconds") != null ? 
            Integer.valueOf(body.get("durationSeconds").toString()) : null;
        Long score = body.get("score") != null ? 
            Long.valueOf(body.get("score").toString()) : null;
        String meta = body.get("meta") != null ? body.get("meta").toString() : null;

        return playHistoryService.recordHistory(userId, gameId, durationSeconds, score, meta);
    }
}
