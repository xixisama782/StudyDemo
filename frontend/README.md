# GameCenter 前端

Vue 3 + Vite + TypeScript + Pinia + Naive UI。

**完整项目文档** → [../PROJECT.md](../PROJECT.md)

## 脚本

```bash
npm install
npm run dev        # 开发，端口 10109
npm run build      # 生产构建
npm run typecheck  # vue-tsc --noEmit
npm run test       # Vitest
```

开发时 `/api`、`/uploads` 代理到 `http://localhost:8080`（见 `vite.config.ts`）。
