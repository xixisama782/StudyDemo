package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.entity.GameType;
import com.example.gamecenter.service.GameTypeService;
import com.example.gamecenter.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 管理端游戏类型 CRUD。 */
@RestController
@RequestMapping(ApiConstants.AdminGameTypes.BASE)
public class AdminGameTypeController {

    @Autowired
    private GameTypeService gameTypeService;

    @PostMapping
    public Result<Object> createGameType(@RequestBody GameType gameType, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        if (gameType.getName() == null || gameType.getCode() == null) {
            return Result.error(
                    ApiBizError.GAME_TYPE_NAME_AND_CODE_REQUIRED.getCode(),
                    ApiBizError.GAME_TYPE_NAME_AND_CODE_REQUIRED.getMessage());
        }
        return gameTypeService.createGameType(gameType);
    }

    @PutMapping("/{id}")
    public Result<Object> updateGameType(@PathVariable Long id, @RequestBody GameType gameType, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return gameTypeService.updateGameType(id, gameType);
    }

    @DeleteMapping("/{id}")
    public Result<Object> deleteGameType(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return gameTypeService.deleteGameType(id);
    }
}
