package com.example.gamecenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gamecenter.entity.GameType;
import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.mapper.GameTypeMapper;
import com.example.gamecenter.service.GameTypeService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 游戏类型业务：全量列表缓存，删除前校验是否仍有关联游戏。 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GameTypeServiceImpl implements GameTypeService {

    @Autowired
    private GameTypeMapper gameTypeMapper;

    @Autowired
    private GameMapper gameMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "gameTypes", key = "'all'")
    public Result<Object> getAllGameTypes() {
        List<GameType> types = gameTypeMapper.selectList(null);
        return Result.success(types);
    }

    @Override
    @CacheEvict(cacheNames = "gameTypes", allEntries = true)
    public Result<Object> createGameType(GameType gameType) {
        if (existsByCode(gameType.getCode())) {
            return Result.error(
                    ApiBizError.GAME_TYPE_CODE_EXISTS.getCode(),
                    ApiBizError.GAME_TYPE_CODE_EXISTS.getMessage());
        }
        // 维护说明：IDE 若标「Unreachable code」多为误报（existsByCode 依赖 DB，静态分析难推断）；以 mvn/运行为准。
        gameTypeMapper.insert(gameType);
        return Result.success("Game type created successfully", gameType);
    }

    @Override
    @CacheEvict(cacheNames = "gameTypes", allEntries = true)
    public Result<Object> updateGameType(Long id, GameType gameType) {
        GameType existing = gameTypeMapper.selectById(id);
        if (existing == null) {
            return Result.error(
                    ApiBizError.GAME_TYPE_NOT_FOUND.getCode(),
                    ApiBizError.GAME_TYPE_NOT_FOUND.getMessage());
        }

        if (gameType.getName() != null) {
            existing.setName(gameType.getName());
        }
        if (gameType.getDescription() != null) {
            existing.setDescription(gameType.getDescription());
        }

        gameTypeMapper.updateById(existing);
        return Result.success("Game type updated successfully", existing);
    }

    @Override
    @CacheEvict(cacheNames = "gameTypes", allEntries = true)
    public Result<Object> deleteGameType(Long id) {
        Long gameCount = gameMapper.countByTypeId(id);
        if (gameCount != null && gameCount > 0) {
            return Result.error(
                    ApiBizError.GAME_TYPE_HAS_GAMES.getCode(),
                    ApiBizError.GAME_TYPE_HAS_GAMES.getMessage());
        }
        // 维护说明：IDE 若标「Unreachable code」多为误报（countByTypeId 为运行时结果）；以 mvn/运行为准。
        gameTypeMapper.deleteById(id);
        return Result.success("Game type deleted successfully", null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<GameType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameType::getCode, code);
        return gameTypeMapper.selectCount(wrapper) > 0;
    }
}
