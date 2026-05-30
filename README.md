# GameCenter

基于 Vue 3 + Spring Boot 3 的小游戏中心：游戏列表、收藏、排行榜、管理后台。

**完整文档** → [PROJECT.md](./PROJECT.md)

## 快速开始（开发）

**前置**：MySQL 8+、Redis、Java 17+、Node 18+

```powershell
# 1. 初始化数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS gamecenter CHARACTER SET utf8mb4;"
mysql -u root -p gamecenter < init_schema.sql

# 2. 一键启动（Redis + 后端 + 前端）
StartGameCenter.cmd
```

- 前端：http://localhost:10109  
- 后端 API：http://localhost:8080/api/game-types  

分步启动、生产部署、环境变量与 FAQ 见 [PROJECT.md](./PROJECT.md)。
