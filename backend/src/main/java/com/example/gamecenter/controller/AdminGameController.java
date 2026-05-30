package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.entity.Game;
import com.example.gamecenter.service.GameService;
import com.example.gamecenter.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 管理端游戏 CRUD。 */
@RestController
@RequestMapping(ApiConstants.AdminGames.BASE)
public class AdminGameController {

    @Autowired
    private GameService gameService;

    @PostMapping
    public Result<Object> createGame(@RequestBody Game game, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return gameService.createGame(game);
    }

    @PutMapping("/{id}")
    public Result<Object> updateGame(@PathVariable Long id, @RequestBody Game game, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return gameService.updateGame(id, game);
    }

    @DeleteMapping("/{id}")
    public Result<Object> deleteGame(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return gameService.deleteGame(id);
    }
}
