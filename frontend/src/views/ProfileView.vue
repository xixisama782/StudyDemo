<template>
  <div class="profile">
    <n-h2 prefix="bar" style="margin-bottom: 20px">个人中心</n-h2>

    <n-spin :show="loading">
      <n-space vertical :size="20">
        <n-card title="用户信息">
          <n-flex :wrap="true" :gap="20" align="center">
            <n-space vertical align="center">
              <n-avatar round :size="80" :src="(userInfo.avatarUrl as string) || undefined">
                {{ avatarLetter }}
              </n-avatar>
            </n-space>
            <div class="grow">
              <n-text strong style="font-size: 18px">
                {{ userInfo.displayName || userInfo.username || '用户' }}
              </n-text>
              <div class="muted">{{ userInfo.email || '-' }}</div>
              <div class="muted">加入于 {{ formatDate(userInfo.createdAt as string) }}</div>
            </div>
            <n-button type="primary" @click="router.push('/app/profile/edit')">编辑资料</n-button>
          </n-flex>
        </n-card>

        <n-card title="游戏数据">
          <n-grid cols="2 s:4" :x-gap="12" :y-gap="12" responsive="screen">
            <n-gi>
              <n-statistic label="总游玩次数" :value="playCountDisplay" />
            </n-gi>
            <n-gi>
              <n-statistic label="总游玩时长" :value="durationLabel" />
            </n-gi>
            <n-gi>
              <n-statistic label="收藏游戏" :value="favStatDisplay" />
            </n-gi>
            <n-gi>
              <n-statistic label="最高排名" :value="bestRankDisplay" />
            </n-gi>
          </n-grid>
        </n-card>

        <n-card title="最近游玩">
          <template #header-extra>
            <n-button quaternary size="small" @click="router.push('/app/history')">查看全部</n-button>
          </template>
          <n-empty v-if="recentHistory.length === 0" description="暂无游玩记录" />
          <n-list v-else>
            <n-list-item v-for="item in recentHistory" :key="recentKey(item)">
              <n-thing>
                <template #header>
                  <n-text strong>{{ item.gameName }}</n-text>
                </template>
                <template #description>
                  {{ item.score }} 分 · {{ formatDurationShort(item.durationSeconds) }}
                </template>
                <template #action>
                  <n-button size="tiny" type="primary" @click="playGame(item)">继续</n-button>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>

        <n-card title="我的收藏">
          <template #header-extra>
            <n-button quaternary size="small" @click="router.push('/app/favorites')">前往查看</n-button>
          </template>
          <n-text>已收藏 {{ favoriteCount }} 款游戏</n-text>
        </n-card>

        <n-card title="我的排行榜">
          <template #header-extra>
            <n-button quaternary size="small" @click="router.push('/leaderboard')">查看全部排行</n-button>
          </template>
          <n-empty v-if="leaderboardPreview.length === 0" description="暂无排名记录" />
          <n-list v-else>
            <n-list-item v-for="item in leaderboardPreview" :key="item.gameId">
              <n-flex justify="space-between">
                <span>{{ item.gameName }}</span>
                <n-space>
                  <n-tag :type="rankTagType(item.rankPosition)">第{{ item.rankPosition }}名</n-tag>
                  <n-text depth="3">{{ item.score }} 分</n-text>
                </n-space>
              </n-flex>
            </n-list-item>
          </n-list>
        </n-card>

        <n-card title="账户设置">
          <n-flex justify="space-between" align="center">
            <n-text>密码与账号安全相关设置</n-text>
            <n-button type="primary" quaternary @click="router.push('/app/profile/security')">前往设置</n-button>
          </n-flex>
        </n-card>
      </n-space>
    </n-spin>
  </div>
</template>

/** 个人中心：资料、统计、最近游玩/收藏/排行预览 */
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NAvatar,
  NButton,
  NCard,
  NEmpty,
  NFlex,
  NGi,
  NGrid,
  NH2,
  NList,
  NListItem,
  NSpin,
  NSpace,
  NStatistic,
  NTag,
  NText,
  NThing
} from 'naive-ui'
import { useAuthStore, type UserInfo } from '../store/auth'
import { userApi, userStatsApi, historyApi, favoriteApi } from '../api'
import type { ApiResult } from '../api'

interface RecentHistoryItem {
  id: number
  gameName: string
  score: number
  durationSeconds?: number
  gameId?: number
}

function recentKey(item: RecentHistoryItem) {
  return item.id
}

function formatDate(dateStr: string | undefined) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

function formatDuration(seconds: number | undefined) {
  if (!seconds || seconds === 0) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

function formatDurationShort(seconds: number | undefined) {
  if (!seconds || seconds === 0) return '0秒'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(true)

const userInfo = ref<Record<string, unknown>>({})
const stats = ref<Record<string, unknown>>({})
const recentHistory = ref<RecentHistoryItem[]>([])
const favoriteCount = ref(0)
const leaderboardPreview = ref<Array<{ gameId: number; gameName: string; rankPosition: number; score: number }>>([])

const avatarLetter = computed(() => {
  const u = userInfo.value.username
  if (typeof u === 'string' && u) return u.charAt(0).toUpperCase()
  return 'U'
})

const rankTagType = (rank: number): 'warning' | 'default' | 'error' | 'info' => {
  if (rank === 1) return 'warning'
  if (rank === 2) return 'default'
  if (rank === 3) return 'error'
  return 'info'
}

const playCountDisplay = computed(() => Number(stats.value.totalPlayCount) || 0)
const durationLabel = computed(() => formatDuration(Number(stats.value.totalDurationSeconds) || 0))
const favStatDisplay = computed(() => Number(stats.value.favoriteCount ?? favoriteCount.value) || 0)
const bestRankDisplay = computed(() => {
  const r = stats.value.bestRank
  if (r === undefined || r === null || r === '') return '-'
  return String(r)
})

/** 并行拉取资料与列表；统计/排行接口失败不阻断页面 */
const fetchAllData = async () => {
  loading.value = true
  try {
    const fallbackUser: ApiResult<Record<string, unknown>> = { code: 200, message: '', data: {} }
    const fallbackHist: ApiResult<{ list: unknown[]; total?: number }> = {
      code: 200,
      message: '',
      data: { list: [], total: 0 }
    }
    const fallbackFav: ApiResult<{ list?: unknown[]; total?: number }> = {
      code: 200,
      message: '',
      data: { list: [], total: 0 }
    }

    const [userRes, historyRes, favRes] = await Promise.all([
      userApi.getMe().catch(() => ({ data: fallbackUser })),
      historyApi.getHistory(null, 1, 3).catch(() => ({ data: fallbackHist })),
      favoriteApi.getFavorites(1, 1).catch(() => ({ data: fallbackFav }))
    ])

    userInfo.value = (userRes.data.data || {}) as Record<string, unknown>
    syncAuthUser(userInfo.value as UserInfo)

    if (historyRes.data.code === 200) {
      recentHistory.value = (historyRes.data.data.list || []) as RecentHistoryItem[]
      stats.value = { ...stats.value, totalPlayCount: historyRes.data.data.total || 0 }
    }

    if (favRes.data.code === 200) {
      favoriteCount.value = favRes.data.data.total || 0
      stats.value = { ...stats.value, favoriteCount: favoriteCount.value }
    }

    try {
      const statsRes = await userStatsApi.getStatistics()
      if (statsRes.data.code === 200) {
        stats.value = { ...stats.value, ...statsRes.data.data }
      }
    } catch {
      /* optional API */
    }

    try {
      const lbRes = await userStatsApi.getLeaderboards()
      if (lbRes.data.code === 200) {
        leaderboardPreview.value = (lbRes.data.data || []) as typeof leaderboardPreview.value
      }
    } catch {
      /* optional API */
    }
  } catch (error) {
    console.error('Failed to load profile data:', error)
  } finally {
    loading.value = false
  }
}

const playGame = (item: RecentHistoryItem) => {
  const id = item.gameId
  if (typeof id === 'number') {
    router.push(`/app/game/${id}`)
  }
}

/** 将接口返回的资料合并进 Pinia，供顶栏等复用 */
const syncAuthUser = (user: UserInfo) => {
  if (!user || !authStore.user) {
    return
  }
  authStore.updateUser({
    ...authStore.user,
    id: (user.id as number) ?? authStore.user.id,
    username: (user.username as string) ?? authStore.user.username,
    email: user.email ?? authStore.user.email,
    displayName: user.displayName ?? authStore.user.displayName,
    avatarUrl: user.avatarUrl ?? authStore.user.avatarUrl
  })
}

onMounted(() => {
  fetchAllData()
})
</script>

<style scoped>
.profile {
  max-width: 820px;
  margin: 0 auto;
  padding: 24px;
}

.grow {
  flex: 1;
  min-width: 200px;
}

.muted {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}
</style>
