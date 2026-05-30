-- GameCenter 全库导出（结构 + 数据）
-- 生成时间: 2026-05-30 10:27:32
-- 来源: localhost:3306 / gamecenter
-- 导入: mysql -u root -p < db/gamecenter_full_export_20260530.sql
-- 说明: 由 mysqldump 生成；会 DROP DATABASE 后重建

-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: gamecenter
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `gamecenter`
--

/*!40000 DROP DATABASE IF EXISTS `gamecenter`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `gamecenter` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `gamecenter`;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admins` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `role` varchar(32) DEFAULT 'admin',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admins`
--

LOCK TABLES `admins` WRITE;
/*!40000 ALTER TABLE `admins` DISABLE KEYS */;
INSERT INTO `admins` VALUES (1,'admin','123456','3516212582@qq.com','GM','Admin','2026-03-14 02:33:06','2026-03-14 02:33:06');
/*!40000 ALTER TABLE `admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorites`
--

DROP TABLE IF EXISTS `favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `game_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_game` (`user_id`,`game_id`),
  KEY `game_id` (`game_id`),
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorites`
--

LOCK TABLES `favorites` WRITE;
/*!40000 ALTER TABLE `favorites` DISABLE KEYS */;
INSERT INTO `favorites` VALUES (3,1,1,'2026-05-16 01:56:33');
/*!40000 ALTER TABLE `favorites` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game_sessions`
--

DROP TABLE IF EXISTS `game_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `game_id` bigint NOT NULL,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ended_at` timestamp NULL DEFAULT NULL,
  `duration_seconds` int DEFAULT '0',
  `score` bigint DEFAULT '0',
  `meta` json DEFAULT NULL,
  `status` enum('active','ended','abandoned') DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `game_id` (`game_id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_user_active` (`user_id`,`status`,`started_at`),
  CONSTRAINT `game_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `game_sessions_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_sessions`
--

LOCK TABLES `game_sessions` WRITE;
/*!40000 ALTER TABLE `game_sessions` DISABLE KEYS */;
INSERT INTO `game_sessions` VALUES (15,1,1,'2026-03-27 12:13:38','2026-03-27 12:13:41',3,0,NULL,'abandoned','2026-03-27 12:13:37','2026-03-27 12:13:41'),(16,1,1,'2026-03-27 12:13:41','2026-03-27 12:13:42',1,0,NULL,'abandoned','2026-03-27 12:13:41','2026-03-27 12:13:42'),(17,1,1,'2026-03-27 12:13:42','2026-03-27 12:13:42',0,0,NULL,'abandoned','2026-03-27 12:13:42','2026-03-27 12:13:42'),(18,1,1,'2026-03-27 12:13:43','2026-03-27 12:14:03',20,0,NULL,'abandoned','2026-03-27 12:13:42','2026-03-27 12:14:03'),(19,1,1,'2026-03-27 12:14:04','2026-03-27 12:14:22',18,0,NULL,'abandoned','2026-03-27 12:14:03','2026-03-27 12:14:22'),(20,1,2,'2026-03-27 12:14:23','2026-03-28 02:16:00',50497,0,NULL,'abandoned','2026-03-27 12:14:22','2026-03-28 02:16:00'),(21,1,1,'2026-03-28 02:16:00','2026-03-28 02:16:14',13,3,NULL,'ended','2026-03-28 02:16:00','2026-03-28 02:16:00'),(22,1,2,'2026-03-28 02:22:24','2026-03-28 02:22:41',17,0,NULL,'ended','2026-03-28 02:22:24','2026-03-28 02:22:24'),(23,1,1,'2026-03-28 02:34:04','2026-03-28 02:34:12',7,0,NULL,'ended','2026-03-28 02:34:03','2026-03-28 02:34:03'),(24,1,2,'2026-03-28 02:34:15','2026-03-28 02:36:00',105,398,NULL,'ended','2026-03-28 02:34:15','2026-03-28 02:34:15'),(25,1,1,'2026-03-28 02:39:35','2026-03-28 02:39:38',2,0,NULL,'ended','2026-03-28 02:39:34','2026-03-28 02:39:34'),(26,1,1,'2026-03-28 02:39:41','2026-03-28 02:39:52',11,2,NULL,'ended','2026-03-28 02:39:40','2026-03-28 02:39:40'),(27,1,1,'2026-05-16 01:38:43','2026-05-16 01:39:16',32,12,NULL,'ended','2026-05-16 01:38:43','2026-05-16 01:38:43'),(28,1,1,'2026-05-16 01:54:46','2026-05-16 01:54:53',6,1,NULL,'ended','2026-05-16 01:54:45','2026-05-16 01:54:45');
/*!40000 ALTER TABLE `game_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game_types`
--

DROP TABLE IF EXISTS `game_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `code` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_types`
--

LOCK TABLES `game_types` WRITE;
/*!40000 ALTER TABLE `game_types` DISABLE KEYS */;
INSERT INTO `game_types` VALUES (1,'快捷小游戏','1','html代码块实现的快捷小游戏','2026-05-16 03:00:11','2026-05-16 03:00:11');
/*!40000 ALTER TABLE `game_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `games` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `type_id` bigint DEFAULT NULL,
  `resource_url` varchar(1024) DEFAULT NULL,
  `thumbnail_url` varchar(1024) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `tags` varchar(255) DEFAULT NULL,
  `controls` text COMMENT '游戏操作说明',
  `is_active` tinyint(1) DEFAULT '1',
  `play_count` bigint DEFAULT '0',
  `last_played_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `type_id` (`type_id`),
  CONSTRAINT `games_ibfk_1` FOREIGN KEY (`type_id`) REFERENCES `game_types` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `games`
--

LOCK TABLES `games` WRITE;
/*!40000 ALTER TABLE `games` DISABLE KEYS */;
INSERT INTO `games` VALUES (1,'贪吃蛇','经典贪吃蛇游戏，玩家控制蛇吃食物来增长自己的身体，避免撞墙或撞到自己',NULL,'/games/snake',NULL,'admin','经典,街机,休闲','方向键或 WASD 控制移动；P 暂停/继续。触屏可使用页面下方方向键。吃到红色食物加分变长，撞墙或撞到自己则结束。',1,3,'2026-05-16 01:54:53','2026-03-16 02:02:51','2026-05-16 01:54:52'),(2,'俄罗斯方块','经典俄罗斯方块游戏，玩家通过旋转和移动下落的方块来消除行',NULL,'/games/tetris',NULL,'admin','经典,街机,益智','←→ 或 A/D 横移；↓ 或 S 软降；↑ 或 X 顺时针旋转；Z 逆时针旋转；空格硬降；P 暂停。触屏可用下方按键。消行得分，堆到顶则结束。',1,1,'2026-03-28 02:22:41','2026-03-16 02:02:51','2026-03-28 02:53:04');
/*!40000 ALTER TABLE `games` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leaderboards`
--

DROP TABLE IF EXISTS `leaderboards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leaderboards` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `game_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `score` bigint NOT NULL,
  `rank_position` int DEFAULT NULL,
  `type` varchar(32) DEFAULT 'all_time',
  `period_start` date DEFAULT NULL,
  `period_end` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leaderboard_game_user_type` (`game_id`,`user_id`,`type`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `leaderboards_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE,
  CONSTRAINT `leaderboards_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leaderboards`
--

LOCK TABLES `leaderboards` WRITE;
/*!40000 ALTER TABLE `leaderboards` DISABLE KEYS */;
INSERT INTO `leaderboards` VALUES (14,1,1,12,2,'all_time',NULL,NULL,'2026-03-28 02:16:14','2026-05-16 01:39:16'),(15,2,1,398,1,'all_time',NULL,NULL,'2026-03-28 02:36:00','2026-03-28 02:36:00');
/*!40000 ALTER TABLE `leaderboards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leaderboards_mig_backup`
--

DROP TABLE IF EXISTS `leaderboards_mig_backup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leaderboards_mig_backup` (
  `id` bigint NOT NULL DEFAULT '0',
  `game_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `score` bigint NOT NULL,
  `rank_position` int DEFAULT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'all_time',
  `period_start` date DEFAULT NULL,
  `period_end` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leaderboards_mig_backup`
--

LOCK TABLES `leaderboards_mig_backup` WRITE;
/*!40000 ALTER TABLE `leaderboards_mig_backup` DISABLE KEYS */;
INSERT INTO `leaderboards_mig_backup` VALUES (14,1,1,3,2,'all_time',NULL,NULL,'2026-03-28 02:16:14','2026-03-28 02:16:14'),(15,2,1,398,1,'all_time',NULL,NULL,'2026-03-28 02:36:00','2026-03-28 02:36:00');
/*!40000 ALTER TABLE `leaderboards_mig_backup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `play_history`
--

DROP TABLE IF EXISTS `play_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `play_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `game_id` bigint NOT NULL,
  `played_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `duration_seconds` int DEFAULT '0',
  `score` bigint DEFAULT '0',
  `meta` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_play_history_user_game` (`user_id`,`game_id`),
  KEY `game_id` (`game_id`),
  CONSTRAINT `play_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `play_history_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `play_history`
--

LOCK TABLES `play_history` WRITE;
/*!40000 ALTER TABLE `play_history` DISABLE KEYS */;
INSERT INTO `play_history` VALUES (1,1,2,'2026-03-28 02:34:15',122,398,NULL),(2,1,1,'2026-05-16 01:54:53',71,1,NULL);
/*!40000 ALTER TABLE `play_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `play_history_mig_backup`
--

DROP TABLE IF EXISTS `play_history_mig_backup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `play_history_mig_backup` (
  `id` bigint NOT NULL DEFAULT '0',
  `user_id` bigint NOT NULL,
  `game_id` bigint NOT NULL,
  `played_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `duration_seconds` int DEFAULT '0',
  `score` bigint DEFAULT '0',
  `meta` json DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `play_history_mig_backup`
--

LOCK TABLES `play_history_mig_backup` WRITE;
/*!40000 ALTER TABLE `play_history_mig_backup` DISABLE KEYS */;
INSERT INTO `play_history_mig_backup` VALUES (12,1,1,'2026-03-28 02:16:00',13,3,NULL),(13,1,2,'2026-03-28 02:22:24',17,0,NULL),(14,1,1,'2026-03-28 02:34:04',7,0,NULL),(15,1,2,'2026-03-28 02:34:15',105,398,NULL),(16,1,1,'2026-03-28 02:39:35',2,0,NULL),(17,1,1,'2026-03-28 02:39:41',11,2,NULL);
/*!40000 ALTER TABLE `play_history_mig_backup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `status` varchar(32) DEFAULT 'normal',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'xi','$2a$10$DLZHlC7Nwo2fHwOK8k7xQukgViykzRNKBSXGxepxVECB2PQO7Xc8i','2878742681@qq.com','熙','/uploads/avatars/avatar_1_20260402081630764.jpg','normal','2026-03-14 00:33:19','2026-03-14 00:33:19'),(2,'xitest','$2a$10$6CM3248frcwQEBqBWkd69.GbJIMnIA2.FoJwUqAo5aOssH8s3ySgm','3516212582@qq.com',NULL,NULL,'normal','2026-05-28 01:17:32','2026-05-28 01:17:32'),(3,'xi2test','$2a$10$r5ltTAktQ5lJwrDMtyxkkOwWUCvGPRRRmXr62NToO8Et.S1U498em','a13773633646@163.com',NULL,NULL,'normal','2026-05-28 01:40:50','2026-05-28 01:40:50');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'gamecenter'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-30 10:27:32
