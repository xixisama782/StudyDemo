package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.AdminStatisticsService;
import com.example.gamecenter.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 管理端运营统计：概览与热门游戏。 */
@RestController
@RequestMapping(ApiConstants.AdminStatistics.BASE)
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    @GetMapping(ApiConstants.AdminStatistics.OVERVIEW)
    public Result<Object> overview(HttpServletRequest request) {
        if (request.getAttribute("adminId") == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return adminStatisticsService.getOverview();
    }

    @GetMapping(ApiConstants.AdminStatistics.POPULAR_GAMES)
    public Result<Object> popularGames(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        if (request.getAttribute("adminId") == null) {
            return Result.error(
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getCode(),
                    ApiBizError.ADMIN_ACCESS_REQUIRED.getMessage());
        }
        return adminStatisticsService.getPopularGames(limit);
    }
}
