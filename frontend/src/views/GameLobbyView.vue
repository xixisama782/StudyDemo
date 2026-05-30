<template>
  <div class="game-lobby">
    <n-space vertical :size="24">
      <n-button quaternary @click="goBack">← 返回</n-button>

      <n-spin v-if="loading" description="加载中..." />

      <n-result v-else-if="errorText" status="error" :title="errorText">
        <template #footer>
          <n-button type="primary" @click="goBack">返回游戏列表</n-button>
        </template>
      </n-result>

      <template v-else>
        <n-card>
          <n-flex :wrap="false" :gap="24" class="game-header">
            <img :src="(game.thumbnailUrl as string) || '/default-game.png'" :alt="String(game.name)" class="game-image" />
            <div class="game-info">
              <n-h2>{{ game.name }}</n-h2>
              <n-text depth="3">{{ game.typeName }}</n-text>
              <n-button type="primary" size="large" style="margin-top: 20px; align-self: flex-start" :loading="starting" @click="startGame">
                {{ starting ? '启动中...' : '开始游戏' }}
              </n-button>
            </div>
          </n-flex>
        </n-card>

        <n-card title="游戏详情">
          <n-descriptions :column="1" label-placement="left" v-if="game.controls">
            <n-descriptions-item label="操作说明">
              {{ game.controls }}
            </n-descriptions-item>
          </n-descriptions>
          <n-h3 prefix="bar">游戏介绍</n-h3>
          <n-text depth="2">{{ game.description || '暂无介绍' }}</n-text>
          <n-h3 prefix="bar" style="margin-top: 16px">游戏信息</n-h3>
          <n-descriptions bordered :column="2" size="small">
            <n-descriptions-item label="游戏类型">{{ game.typeName }}</n-descriptions-item>
            <n-descriptions-item label="游玩次数">{{ game.playCount || 0 }} 次</n-descriptions-item>
            <n-descriptions-item label="提供者">{{ game.provider || '官方' }}</n-descriptions-item>
            <n-descriptions-item label="标签">{{ game.tags || '无' }}</n-descriptions-item>
          </n-descriptions>
        </n-card>
      </template>
    </n-space>
  </div>
</template>

/** 游戏详情/大厅：展示信息并通过 startGameSession 进入游玩页 */
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NButton,
  NCard,
  NDescriptions,
  NDescriptionsItem,
  NFlex,
  NH2,
  NH3,
  NResult,
  NSpin,
  NSpace,
  NText,
  useMessage
} from 'naive-ui'
import { gameApi, historyApi } from '../api'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const game = ref<Record<string, unknown>>({})
const loading = ref(true)
const errorText = ref('')
const starting = ref(false)

const goBack = () => {
  router.push('/app')
}

const loadGame = async () => {
  const gameId = route.params.id
  const idStr = Array.isArray(gameId) ? gameId[0] : gameId
  if (!idStr) {
    errorText.value = '游戏ID不存在'
    loading.value = false
    return
  }

  try {
    const response = await gameApi.getGameById(idStr)
    if (response.data.code === 200) {
      game.value = (response.data.data || {}) as Record<string, unknown>
    } else {
      errorText.value = getApiBusinessMessage(response, '加载游戏信息失败')
    }
  } catch (e) {
    errorText.value = getApiErrorMessage(e, '网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

/** 创建会话并写入 sessionStorage，再跳转 /play */
const startGame = async () => {
  starting.value = true
  const id = game.value.id as number | undefined
  if (id == null) {
    starting.value = false
    return
  }

  try {
    const response = await historyApi.startGameSession(id)
    if (response.data.code === 200) {
      const sessionData = response.data.data
      sessionStorage.setItem('gameSessionId', String(sessionData.sessionId))
      if (sessionData.startedAt) sessionStorage.setItem('gameStartedAt', sessionData.startedAt)
      router.push(`/app/game/${id}/play`)
    } else {
      message.error(getApiBusinessMessage(response, '启动游戏失败'))
    }
  } catch (e) {
    message.error(getApiErrorMessage(e, '网络错误，请稍后重试'))
  } finally {
    starting.value = false
  }
}

onMounted(() => {
  loadGame()
})
</script>

<style scoped>
.game-lobby {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.game-header {
  align-items: flex-start;
}

@media (max-width: 768px) {
  .game-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

.game-image {
  width: 240px;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  background: var(--color-bg-tertiary);
  flex-shrink: 0;
}

.game-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
