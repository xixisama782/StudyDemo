-- =============================================================================
-- leaderboards：合并重复 (game_id, user_id, type)，保留最高分行（同分保留 id 最大）
-- 然后添加 UNIQUE KEY uk_leaderboard_game_user_type
-- 依赖：MySQL 8.0+（与应用窗口函数查询一致）
-- =============================================================================

USE gamecenter;

DROP TABLE IF EXISTS leaderboards_mig_backup;
CREATE TABLE leaderboards_mig_backup AS SELECT * FROM leaderboards;

DELETE FROM leaderboards WHERE id NOT IN (
  SELECT keep_id FROM (
    SELECT MAX(l.id) AS keep_id
    FROM leaderboards l
    INNER JOIN (
      SELECT game_id, user_id, type, MAX(score) AS mx
      FROM leaderboards
      GROUP BY game_id, user_id, type
    ) t ON l.game_id = t.game_id
       AND l.user_id = t.user_id
       AND l.type = t.type
       AND l.score = t.mx
    GROUP BY l.game_id, l.user_id, l.type
  ) x
);

-- 若索引已存在则跳过（手动执行时需注释掉下面 ALTER 一次）
ALTER TABLE leaderboards
  ADD UNIQUE KEY uk_leaderboard_game_user_type (game_id, user_id, type);

ALTER TABLE leaderboards AUTO_INCREMENT = 1;

SELECT 'migration_leaderboards_unique: done' AS status;
