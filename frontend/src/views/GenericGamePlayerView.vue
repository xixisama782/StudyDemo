<template>
  <div class="game-player" :class="{ 'game-player--immersive': immersive }">
    <div class="game-header">
      <n-button quaternary class="back-btn" @click="goBack">← 返回</n-button>
      <h2>{{ gameName || '加载中…' }}</h2>
      <div class="header-actions">
        <n-button
          v-if="iframeSrc && !error"
          size="small"
          secondary
          :aria-pressed="immersive"
          :title="immersive ? '显示操作说明' : '专注模式（隐藏说明）'"
          @click="immersive = !immersive"
        >
          {{ immersive ? '说明' : '专注' }}
        </n-button>
      </div>
    </div>

    <div v-if="error" class="error-state">
      <p>{{ error }}</p>
      <n-button type="primary" @click="goBack">返回游戏详情</n-button>
    </div>

    <div v-else class="game-layout" :class="{ 'game-layout--immersive': immersive }">
      <div class="iframe-shell" @click="focusGameFrame">
        <p v-if="iframeSrc && showFocusHint" class="focus-hint" role="status">点击游戏区域以使用键盘操作</p>
        <iframe
          v-if="iframeSrc"
          ref="gameFrame"
          :key="iframeKey"
          :src="iframeSrc"
          class="game-iframe"
          title="游戏画布"
          tabindex="-1"
          allow="fullscreen"
          sandbox="allow-scripts allow-same-origin"
          @load="onIframeLoad"
        />
      </div>
      <aside v-if="gameControls && !immersive" class="game-sidebar">
        <h3>操作说明</h3>
        <p class="controls-text">{{ gameControls }}</p>
      </aside>
    </div>

    <Teleport to="body">
      <div
        v-if="gameOverModalOpen"
        class="gc-modal-overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="gc-game-over-title"
        @click.self="closeGameOverModal"
      >
        <div class="gc-modal">
          <h3 id="gc-game-over-title" class="gc-modal-title">游戏结束</h3>
          <p class="gc-modal-score">
            得分 <strong>{{ gameOverScore }}</strong>
            <template v-if="gameOverLines != null">
              <span class="gc-modal-meta">· 消除行数 {{ gameOverLines }}</span>
            </template>
          </p>
          <p class="gc-modal-hint">成绩已记录。若要再次参与排行，请返回详情页重新点击「开始游戏」。</p>
          <div class="gc-modal-actions">
            <n-button @click="closeGameOverModal">关闭</n-button>
            <n-button type="primary" @click="goBack">返回游戏详情</n-button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

/**
 * 通用游戏播放器：iframe 加载资源，监听 postMessage GAME_OVER，
 * 结束会话、上报成绩/排行；离开页时用 sendBeacon 兜底结束会话
 */
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton } from 'naive-ui'
import { gameApi, historyApi, playApi, leaderboardApi } from '../api'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

interface GameOverMessage {
  type: string
  score?: number | string
  lines?: number
}

/** 将 resourceUrl 规范为可嵌入的 index.html 地址 */
function resolveIframeSrc(resourceUrl: unknown) {
  if (!resourceUrl) return ''
  const u = String(resourceUrl).trim()
  if (/^https?:\/\//i.test(u)) return u
  const base = u.replace(/\/$/, '')
  if (base.endsWith('.html')) return base
  return `${base}/index.html`
}

const route = useRoute()
const router = useRouter()

const gameFrame = ref<HTMLIFrameElement | null>(null)
const gameId = ref(0)
const gameName = ref('')
const gameControls = ref('')
const iframeSrc = ref('')
const iframeKey = ref(0)
const error = ref('')
const immersive = ref(false)
const showFocusHint = ref(true)

const gameOverModalOpen = ref(false)
const gameOverScore = ref(0)
const gameOverLines = ref<number | null>(null)

const sessionId = ref<number | null>(null)
const gameOverHandled = ref(false)
const lastScore = ref(0)
const beaconSent = ref(false)

const goBack = () => {
  router.push(`/app/game/${gameId.value}`)
}

const focusGameFrame = () => {
  try {
    const el = gameFrame.value
    if (el?.contentWindow) {
      el.contentWindow.focus()
      showFocusHint.value = false
    }
  } catch {
    /* cross-origin */
  }
}

const onIframeLoad = () => {
  focusGameFrame()
}

const closeGameOverModal = () => {
  gameOverModalOpen.value = false
}

/** 未正常 GAME_OVER 时，页面卸载用 Beacon 结束会话 */
const handleBeacon = () => {
  if (beaconSent.value || gameOverHandled.value) return
  const sid = sessionStorage.getItem('gameSessionId')
  if (!sid) return
  const token = localStorage.getItem('token')
  if (!token) return
  beaconSent.value = true
  const payload = JSON.stringify({
    sessionId: Number(sid),
    score: lastScore.value || 0,
    token
  })
  const blob = new Blob([payload], { type: 'application/json' })
  navigator.sendBeacon('/api/users/me/history/session/beacon/end', blob)
}

const handleBeforeUnload = () => {
  handleBeacon()
}

/** 同源 iframe 发来的 GAME_OVER：结束会话并提交分数 */
const handleGameMessage = async (event: MessageEvent) => {
  const expectedOrigin = window.location.origin
  if (event.origin !== expectedOrigin) return
  const data = event.data as GameOverMessage | null
  if (!data || data.type !== 'GAME_OVER') return
  if (gameOverHandled.value) return

  const score = typeof data.score === 'number' ? data.score : Number(data.score) || 0
  lastScore.value = score
  gameOverHandled.value = true

  gameOverScore.value = score
  gameOverLines.value = typeof data.lines === 'number' ? data.lines : null
  gameOverModalOpen.value = true

  const sid = sessionId.value
  if (!sid) {
    console.warn('缺少 sessionId，无法结束会话')
    return
  }

  sessionStorage.removeItem('gameSessionId')
  sessionStorage.removeItem('gameStartedAt')

  try {
    const endRes = await historyApi.endGameSession(sid, { score })
    if (endRes.data.code !== 200) {
      console.warn('结束会话失败', endRes.data)
      return
    }
    const gid = gameId.value
    const tasks = [playApi.recordPlay(gid, { score })]
    if (score > 0) {
      tasks.unshift(leaderboardApi.submitScore(gid, { score }))
    }
    await Promise.allSettled(tasks.map((p) => p.catch((e) => console.error(e))))
  } catch (e) {
    console.error('结束会话或上报成绩失败:', getApiErrorMessage(e, '请求失败'), e)
  }
}

onMounted(async () => {
  const idParam = route.params.id
  const parsed = parseInt(String(idParam), 10)
  if (!idParam || Number.isNaN(parsed)) {
    error.value = '游戏 ID 无效'
    return
  }
  gameId.value = parsed

  const sidRaw = sessionStorage.getItem('gameSessionId')
  if (!sidRaw) {
    error.value = '未找到游戏会话，请从游戏详情页点击「开始游戏」'
    return
  }
  sessionId.value = parseInt(sidRaw, 10)

  try {
    const res = await gameApi.getGameById(gameId.value)
    if (res.data.code !== 200 || !res.data.data) {
      error.value = getApiBusinessMessage(res, '加载游戏失败')
      return
    }
    const g = res.data.data as Record<string, unknown>
    gameName.value = String(g.name || '')
    gameControls.value = String(g.controls || '')
    iframeSrc.value = resolveIframeSrc(g.resourceUrl)
    if (!iframeSrc.value) {
      error.value = '该游戏未配置资源地址（resourceUrl）'
    }
  } catch (e) {
    error.value = getApiErrorMessage(e, '网络错误，请稍后重试')
  }

  window.addEventListener('message', handleGameMessage)
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  window.removeEventListener('message', handleGameMessage)
  window.removeEventListener('beforeunload', handleBeforeUnload)
  handleBeacon()
})
</script>

<style scoped>
.game-player {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 16px 24px;
  min-height: 0;
}

.game-player--immersive {
  max-width: 100%;
}

.game-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 16px;
  min-height: 44px;
}

.game-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

@media (min-width: 641px) {
  .game-header h2 {
    font-size: 24px;
  }

  .game-player {
    padding: 24px;
  }

  .game-header {
    margin-bottom: 24px;
  }
}

.header-actions {
  position: absolute;
  right: 0;
  display: flex;
  gap: 8px;
}

.back-btn {
  position: absolute;
  left: 0;
}

.error-state {
  text-align: center;
  padding: 48px 16px;
  color: #9ca3af;
  font-size: 14px;
}

.game-layout {
  display: flex;
  gap: 24px;
  justify-content: center;
  align-items: flex-start;
}

.game-layout--immersive {
  gap: 0;
}

.iframe-shell {
  position: relative;
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.focus-hint {
  margin: 0 0 8px;
  padding: 8px 12px;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  background: rgba(20, 24, 28, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  max-width: 100%;
}

.game-iframe {
  flex: 1 1 auto;
  width: 100%;
  max-width: min(100%, 920px);
  min-height: min(72dvh, 640px);
  border: 2px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  background: #1e2329;
}

.game-layout--immersive .game-iframe {
  max-width: 100%;
  min-height: min(85dvh, 900px);
}

.game-sidebar {
  flex: 0 0 260px;
  padding: 16px;
  background: #14181c;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
}

.game-sidebar h3 {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 12px;
}

.controls-text {
  font-size: 14px;
  color: #9ca3af;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .game-layout {
    flex-direction: column;
    align-items: stretch;
  }

  .game-sidebar {
    flex: none;
    width: 100%;
    order: -1;
  }

  .game-iframe {
    min-height: min(65dvh, 560px);
  }
}

@media (max-width: 640px) {
  .game-header h2 {
    font-size: 16px;
    padding: 0 72px;
    text-align: center;
  }

  .game-iframe {
    min-height: min(58dvh, 520px);
  }
}
</style>

<style>
.gc-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 4000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  padding-bottom: max(16px, env(safe-area-inset-bottom));
  background: rgba(0, 0, 0, 0.5);
  animation: gc-modal-fade 0.25s ease-out;
}

@keyframes gc-modal-fade {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.gc-modal {
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow: auto;
  padding: 24px;
  background: #14181c;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.45);
  animation: gc-modal-pop 0.25s ease-out;
}

@keyframes gc-modal-pop {
  from {
    opacity: 0;
    transform: scale(0.96);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.gc-modal-title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
}

.gc-modal-score {
  margin: 0 0 12px;
  font-size: 16px;
  color: #9ca3af;
}

.gc-modal-score strong {
  font-size: 24px;
  font-weight: 700;
  color: #7c3aed;
}

.gc-modal-meta {
  font-size: 14px;
  color: #6b7280;
}

.gc-modal-hint {
  margin: 0 0 24px;
  font-size: 14px;
  line-height: 1.6;
  color: #6b7280;
}

.gc-modal-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: flex-end;
}
</style>
