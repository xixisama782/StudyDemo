-- GameCenter 初始化数据库脚本

-- 1) 创建数据库
CREATE DATABASE IF NOT EXISTS gamecenter
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE gamecenter;

-- 2) 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE,
  display_name VARCHAR(100),
  avatar_url VARCHAR(255),
  status VARCHAR(32) DEFAULT 'normal',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) 管理员表
CREATE TABLE IF NOT EXISTS admins (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  display_name VARCHAR(100),
  role VARCHAR(32) DEFAULT 'admin',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) 游戏类型表
CREATE TABLE IF NOT EXISTS game_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) 游戏目录表
CREATE TABLE IF NOT EXISTS games (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  type_id BIGINT,
  resource_url VARCHAR(1024),
  thumbnail_url VARCHAR(1024),
  provider VARCHAR(255),
  tags VARCHAR(255),
  controls TEXT DEFAULT NULL COMMENT '游戏操作说明',
  is_active TINYINT(1) DEFAULT 1,
  play_count BIGINT DEFAULT 0,
  last_played_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (type_id) REFERENCES game_types(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) 收藏表
CREATE TABLE IF NOT EXISTS favorites (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  game_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_game (user_id, game_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) 游玩历史记录表（每用户每游戏至多一行；重复游玩时应用层 upsert 累加时长、覆盖分数等）
CREATE TABLE IF NOT EXISTS play_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  game_id BIGINT NOT NULL,
  played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  duration_seconds INT DEFAULT 0,
  score BIGINT DEFAULT 0,
  meta JSON DEFAULT NULL,
  UNIQUE KEY uk_play_history_user_game (user_id, game_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) 排行榜表（每游戏每用户每种 type 至多一行；名次由查询侧 RANK() 动态计算）
CREATE TABLE IF NOT EXISTS leaderboards (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  game_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  score BIGINT NOT NULL,
  rank_position INT NULL COMMENT '遗留列，可由应用空置；名次以查询计算为准',
  type VARCHAR(32) DEFAULT 'all_time',
  period_start DATE NULL,
  period_end DATE NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_leaderboard_game_user_type (game_id, user_id, type),
  FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 添加游戏数据
INSERT INTO games (name, description, type_id, resource_url, thumbnail_url, provider, tags, is_active, play_count, controls, created_at, updated_at) VALUES
('贪吃蛇', '经典贪吃蛇游戏，玩家控制蛇吃食物来增长自己的身体，避免撞墙或撞到自己', NULL, '/games/snake', NULL, 'admin', '经典,街机,休闲', 1, 0, '方向键或 WASD 控制移动；P 暂停/继续。触屏可使用页面下方方向键。吃到红色食物加分变长，撞墙或撞到自己则结束。', NOW(), NOW()),
('俄罗斯方块', '经典俄罗斯方块游戏，玩家通过旋转和移动下落的方块来消除行', NULL, '/games/tetris', NULL, 'admin', '经典,街机,益智', 1, 0, '←→ 或 A/D 横移；↓ 或 S 软降；↑ 或 X 顺时针旋转；Z 逆时针旋转；空格硬降；P 暂停。触屏可用下方按键。消行得分，堆到顶则结束。', NOW(), NOW());

-- 9) 游戏会话表 (Stage 3: 游戏时长精确统计功能)
CREATE TABLE IF NOT EXISTS game_sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  game_id BIGINT NOT NULL,
  started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at TIMESTAMP NULL,
  duration_seconds INT DEFAULT 0,
  score BIGINT DEFAULT 0,
  meta JSON DEFAULT NULL,
  status ENUM('active', 'ended', 'abandoned') DEFAULT 'active',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
  INDEX idx_user_status (user_id, status),
  INDEX idx_user_active (user_id, status, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE games ADD COLUMN controls TEXT DEFAULT NULL COMMENT '游戏操作说明' AFTER tags;