# GameCenter 用户端前端

Vue 3 + Vite + TypeScript + Pinia + Naive UI。独立运行于端口 **10109**；管理端见 `../admin-frontend`（10110）。

**完整项目文档** → [../PROJECT.md](../PROJECT.md)

## 脚本

```bash
npm install
npm run dev          # 开发，端口 10109
npm run dev:client   # 同 dev
npm run build        # 生产构建 → dist/
npm run typecheck
npm run test
```

开发时 `/api`、`/uploads` 代理到 `http://localhost:8080`（见 `vite.config.ts`）。
