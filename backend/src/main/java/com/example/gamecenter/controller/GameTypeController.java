package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.GameTypeService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 公开游戏类型列表。 */
@RestController
@RequestMapping(ApiConstants.GameTypes.BASE)
public class GameTypeController {

    @Autowired
    private GameTypeService gameTypeService;

    @GetMapping
    public Result<Object> getGameTypes() {
        return gameTypeService.getAllGameTypes();
    }
}
