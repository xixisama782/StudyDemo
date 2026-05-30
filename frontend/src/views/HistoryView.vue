<template>
  <div class="history">
    <n-h2 style="margin-bottom: 16px">游玩历史</n-h2>
    <n-spin :show="loading">
      <n-empty v-if="!loading && history.length === 0" description="暂无游玩记录">
        <template #extra>
          <n-button type="primary" @click="goToGames">开始游戏</n-button>
        </template>
      </n-empty>
      <n-list v-else bordered>
        <n-list-item v-for="item in history" :key="item.id">
          <n-thing>
            <template #avatar>
              <div class="cover" @click="playGame(item)">
                <img v-if="item.thumbnailUrl" :src="item.thumbnailUrl" :alt="item.gameName" />
                <span v-else class="placeholder">
                  <n-icon :size="24">
                    <game-controller-outline />
                  </n-icon>
                </span>
              </div>
            </template>
            <template #header>
              <n-text strong style="cursor: pointer" @click="playGame(item)">{{ item.gameName }}</n-text>
            </template>
            <template #description>
              <n-space :size="12">
                <n-text depth="3">
                  <n-space align="center" :size="4">
                    <n-icon :size="14"><time-outline /></n-icon>
                    <span>{{ formatDuration(item.durationSeconds) }}</span>
                  </n-space>
                </n-text>
                <n-text depth="3">
                  <n-space align="center" :size="4">
                    <n-icon :size="14"><trophy-outline /></n-icon>
                    <span>{{ item.score }}分</span>
                  </n-space>
                </n-text>
              </n-space>
              <n-text depth="3" tag="div" style="margin-top: 4px">{{ formatDate(item.playedAt) }}</n-text>
            </template>
            <template #action>
              <n-button size="small" type="primary" @click="playGame(item)">再玩一次</n-button>
            </template>
          </n-thing>
        </n-list-item>
      </n-list>
    </n-spin>
    <n-space v-if="total > pageSize" justify="center" style="margin-top: 24px">
      <n-button :disabled="page <= 1" @click="changePage(page - 1)">上一页</n-button>
      <n-text depth="3">{{ page }} / {{ totalPages }}</n-text>
      <n-button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</n-button>
    </n-space>
  </div>
</template>

/** 游玩历史：分页展示时长、得分与再玩入口 */
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NEmpty, NH2, NIcon, NList, NListItem, NSpin, NSpace, NText, NThing } from 'naive-ui'
import { GameControllerOutline, TimeOutline, TrophyOutline } from '@vicons/ionicons5'
import { historyApi } from '../api'
import { getApiErrorMessage } from '../utils/apiError'

interface HistoryRow {
  id: number
  gameId: number
  gameName: string
  thumbnailUrl?: string
  durationSeconds?: number
  score?: number
  playedAt?: string
}

const router = useRouter()
const history = ref<HistoryRow[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const fetchHistory = async () => {
  loading.value = true
  try {
    const response = await historyApi.getHistory(null, page.value, pageSize.value)
    if (response.data.code === 200) {
      history.value = (response.data.data.list || []) as HistoryRow[]
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取历史记录失败:', getApiErrorMessage(error, '请求失败'), error)
  } finally {
    loading.value = false
  }
}

const changePage = (newPage: number) => {
  page.value = newPage
  fetchHistory()
}

const playGame = (item: HistoryRow) => {
  if (item.gameId) {
    router.push(`/app/game/${item.gameId}`)
  }
}

const goToGames = () => {
  router.push('/app')
}

const formatDuration = (seconds: number | undefined) => {
  if (!seconds || seconds === 0) return '0秒'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  if (minutes > 0) {
    return `${minutes}分${secs}秒`
  }
  return `${secs}秒`
}

const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.cover {
  width: 88px;
  height: 66px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: var(--color-bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder {
  font-size: 24px;
  opacity: 0.5;
}
</style>
