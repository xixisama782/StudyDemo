-- =============================================================================
-- play_history：每用户每游戏唯一一行；合并历史数据后添加唯一索引
-- 合并规则：duration_seconds = SUM(各次)；score/meta/played_at 取 id 最大的一条（最近一次）
-- 执行：mysql -u root -p gamecenter < migration_play_history_unique.sql
-- 或：.\db\run_play_history_migration.ps1
-- =============================================================================

USE gamecenter;

-- 备份整表（可随时 DROP play_history_mig_backup 释放空间）
DROP TABLE IF EXISTS play_history_mig_backup;
CREATE TABLE play_history_mig_backup AS SELECT * FROM play_history;

-- 清空后按 (user_id, game_id) 合并写回
TRUNCATE TABLE play_history;

INSERT INTO play_history (user_id, game_id, played_at, duration_seconds, score, meta)
SELECT
  g.user_id,
  g.game_id,
  g.max_played,
  g.total_duration,
  h.score,
  h.meta
FROM (
  SELECT
    user_id,
    game_id,
    COALESCE(SUM(duration_seconds), 0) AS total_duration,
    MAX(played_at) AS max_played
  FROM play_history_mig_backup
  GROUP BY user_id, game_id
) g
INNER JOIN (
  SELECT ph.user_id, ph.game_id, ph.score, ph.meta
  FROM play_history_mig_backup ph
  INNER JOIN (
    SELECT user_id, game_id, MAX(id) AS max_id
    FROM play_history_mig_backup
    GROUP BY user_id, game_id
  ) latest ON ph.id = latest.max_id
) h ON h.user_id = g.user_id AND h.game_id = g.game_id;

-- 唯一约束：同一用户同一游戏仅一行
ALTER TABLE play_history
  ADD UNIQUE KEY uk_play_history_user_game (user_id, game_id);

SELECT 'migration_play_history_unique: done' AS status;
