package com.example.gamecenter.service.impl;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.entity.Game;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.mapper.GameTypeMapper;
import com.example.gamecenter.service.GameService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 游戏业务：公开列表 Redis 缓存，写操作全量失效缓存。 */
@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private GameTypeMapper gameTypeMapper;

    @Override
    @Cacheable(cacheNames = "games", keyGenerator = "gameListKeyGenerator")
    public Result<Object> getGameList(Long typeId, String keyword, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Integer offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = gameMapper.selectGameList(typeId, keyword, offset, pageSize);
        Long total = gameMapper.selectGameCount(typeId, keyword);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("page", page);
        resultMap.put("pageSize", pageSize);
        resultMap.put("list", list);

        return Result.success(resultMap);
    }

    @Override
    @Cacheable(cacheNames = "games", key = "'detail:' + #id", unless = "#result == null || #result.code != 200")
    public Result<Object> getGameById(Long id) {
        Map<String, Object> game = gameMapper.selectGameById(id);
        if (game == null) {
            return Result.error(ApiBizError.GAME_NOT_FOUND.getCode(), ApiBizError.GAME_NOT_FOUND.getMessage());
        }
        return Result.success(game);
    }

    @Override
    public Result<Object> getAdminGameList(Long typeId, String keyword, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        Integer offset = (page - 1) * pageSize;
        List<Map<String, Object>> list = gameMapper.selectAdminGameList(typeId, keyword, offset, pageSize);
        Long total = gameMapper.selectAdminGameCount(typeId, keyword);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("page", page);
        resultMap.put("pageSize", pageSize);
        resultMap.put("list", list);

        return Result.success(resultMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "games", allEntries = true)
    public Result<Object> createGame(Game game) {
        if (game.getName() == null || game.getTypeId() == null) {
            return Result.error(
                    ApiBizError.GAME_NAME_TYPE_REQUIRED.getCode(),
                    ApiBizError.GAME_NAME_TYPE_REQUIRED.getMessage());
        }

        if (game.getTypeId() != null) {
            if (gameTypeMapper.selectById(game.getTypeId()) == null) {
                return Result.error(
                        ApiBizError.GAME_TYPE_NOT_FOUND.getCode(),
                        ApiBizError.GAME_TYPE_NOT_FOUND.getMessage());
            }
        }

        if (game.getIsActive() == null) {
            game.setIsActive(true);
        }
        if (game.getPlayCount() == null) {
            game.setPlayCount(0L);
        }

        gameMapper.insert(game);
        return Result.success("Game created successfully", game);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "games", allEntries = true)
    public Result<Object> updateGame(Long id, Game game) {
        Game existing = gameMapper.selectById(id);
        if (existing == null) {
            return Result.error(ApiBizError.GAME_NOT_FOUND.getCode(), ApiBizError.GAME_NOT_FOUND.getMessage());
        }

        if (game.getName() != null) {
            existing.setName(game.getName());
        }
        if (game.getDescription() != null) {
            existing.setDescription(game.getDescription());
        }
        if (game.getTypeId() != null) {
            if (gameTypeMapper.selectById(game.getTypeId()) == null) {
                return Result.error(
                        ApiBizError.GAME_TYPE_NOT_FOUND.getCode(),
                        ApiBizError.GAME_TYPE_NOT_FOUND.getMessage());
            }
            existing.setTypeId(game.getTypeId());
        }
        if (game.getResourceUrl() != null) {
            existing.setResourceUrl(game.getResourceUrl());
        }
        if (game.getThumbnailUrl() != null) {
            existing.setThumbnailUrl(game.getThumbnailUrl());
        }
        if (game.getProvider() != null) {
            existing.setProvider(game.getProvider());
        }
        if (game.getTags() != null) {
            existing.setTags(game.getTags());
        }
        if (game.getIsActive() != null) {
            existing.setIsActive(game.getIsActive());
        }

        gameMapper.updateById(existing);
        return Result.success("Game updated successfully", existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "games", allEntries = true)
    public Result<Object> deleteGame(Long id) {
        Game game = gameMapper.selectById(id);
        if (game == null) {
            return Result.error(ApiBizError.GAME_NOT_FOUND.getCode(), ApiBizError.GAME_NOT_FOUND.getMessage());
        }

        gameMapper.deleteById(id);
        return Result.success("Game deleted successfully", null);
    }
}
