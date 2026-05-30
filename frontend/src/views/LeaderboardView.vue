<template>
  <div class="leaderboard">
    <n-space vertical :size="20">
      <n-flex align="center" :gap="16">
        <n-button quaternary @click="goBack">← 返回</n-button>
        <n-h2 style="margin: 0; flex: 1; text-align: center">排行榜</n-h2>
      </n-flex>

      <n-spin v-if="gameLoading" description="游戏加载中..." />
      <n-empty v-else-if="games.length === 0" description="暂无可用游戏" />

      <template v-else>
        <n-select
          v-model:value="selectedGameId"
          :options="gameOptions"
          placeholder="选择游戏"
          style="max-width: 360px"
          @update:value="onGameChange"
        />

        <n-card v-if="selectedGameId">
          <n-radio-group v-model:value="selectedType" size="medium" @update:value="onTypeChange">
            <n-radio-button value="daily">今日榜</n-radio-button>
            <n-radio-button value="weekly">周榜</n-radio-button>
            <n-radio-button value="all_time">总榜</n-radio-button>
          </n-radio-group>

          <n-spin :show="loading" style="margin-top: 16px">
            <n-empty v-if="!loading && entries.length === 0" :description="emptyHint" />
            <n-data-table v-else :columns="columns" :data="visibleEntries" :bordered="false" size="small" />
          </n-spin>

          <n-space v-if="entries.length > 0" justify="center" style="margin-top: 16px">
            <n-button :disabled="page <= 1" size="small" @click="changePage(page - 1)">上一页</n-button>
            <n-text depth="3">第 {{ page }} 页</n-text>
            <n-button :disabled="!hasNextPage" size="small" @click="changePage(page + 1)">下一页</n-button>
          </n-space>

          <n-alert v-if="myRank" type="info" style="margin-top: 16px" :show-icon="false">
            我的排名: 第 {{ myRank.rankPosition }} 名 · {{ myRank.score }} 分
          </n-alert>
          <n-text v-else-if="!authStore.isUser" depth="3" style="display: block; margin-top: 12px; text-align: center">
            登录普通用户后可查看我的排名。
          </n-text>
          <n-text v-else depth="3" style="display: block; margin-top: 12px; text-align: center">
            当前游戏暂无我的排名记录。
          </n-text>
        </n-card>
      </template>
    </n-space>
  </div>
</template>

/** 排行榜：选游戏与榜单类型，展示榜单及当前用户名次 */
<script setup lang="ts">
import { computed, h, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NAlert,
  NButton,
  NCard,
  NDataTable,
  NEmpty,
  NFlex,
  NH2,
  NIcon,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NSpin,
  NSpace,
  NTag,
  NText,
  type DataTableColumns
} from 'naive-ui'
import { PersonOutline } from '@vicons/ionicons5'
import { gameApi, leaderboardApi } from '../api'
import { useAuthStore } from '../store/auth'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

interface GameOpt {
  id: number
  name: string
}

interface EntryRow {
  userId: number
  username: string
  score: number
  rankPosition: number
  avatarUrl?: string
  isMe?: boolean
}

const router = useRouter()
const authStore = useAuthStore()
const games = ref<GameOpt[]>([])
const selectedGameId = ref<number | null>(null)
const selectedType = ref('all_time')
const entries = ref<EntryRow[]>([])
const myRank = ref<{ rankPosition: number; score: number } | null>(null)
const loading = ref(false)
const gameLoading = ref(false)
const errorMessage = ref('')
const page = ref(1)
const pageSize = ref(10)

const gameOptions = computed(() => games.value.map((g) => ({ label: g.name, value: g.id })))

const visibleEntries = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return entries.value.slice(start, start + pageSize.value)
})

const hasNextPage = computed(() => entries.value.length > page.value * pageSize.value)

const emptyHint = computed(() => {
  if (errorMessage.value) return errorMessage.value
  const labels: Record<string, string> = {
    daily: '今日还没有玩家上榜，去完成一局游戏吧！',
    weekly: '本周还没有玩家上榜，去完成一局游戏吧！',
    all_time: '成为第一个上榜的玩家吧！'
  }
  return labels[selectedType.value] || labels.all_time
})

const columns: DataTableColumns<EntryRow> = [
  {
    title: '排名',
    key: 'rankPosition',
    width: 80,
    render: (row) => {
      if (row.rankPosition <= 3) {
        const type = row.rankPosition === 1 ? 'warning' : row.rankPosition === 2 ? 'default' : 'error'
        return h(
          NTag,
          { type, round: true, size: 'small' },
          { default: () => String(row.rankPosition) }
        )
      }
      return row.rankPosition
    }
  },
  {
    title: '玩家',
    key: 'username',
    render: (row) =>
      h(
        NSpace,
        { align: 'center', size: 8 },
        {
          default: () => [
            row.avatarUrl
              ? h('img', { src: row.avatarUrl, class: 'lb-avatar' })
              : h(
                  NIcon,
                  { class: 'lb-ph', size: 18 },
                  { default: () => h(PersonOutline) }
                ),
            h('span', null, row.username),
            row.isMe ? h(NTag, { type: 'primary', size: 'small' }, { default: () => '我' }) : null
          ]
        }
      )
  },
  {
    title: '得分',
    key: 'score',
    render: (row) => h(NText, { strong: true }, { default: () => `${row.score} 分` })
  }
]

const goBack = () => {
  router.push('/app')
}

const fetchGames = async () => {
  gameLoading.value = true
  try {
    const response = await gameApi.getGameList(null, null, 1, 100)
    if (response.data.code === 200) {
      games.value = (response.data.data.list || []) as GameOpt[]
      selectedGameId.value = games.value[0]?.id ?? null
    } else {
      errorMessage.value = getApiBusinessMessage(response, '加载游戏失败')
    }
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '加载游戏失败')
    console.error(error)
  } finally {
    gameLoading.value = false
  }
}

const fetchLeaderboard = async () => {
  if (selectedGameId.value == null) return
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await leaderboardApi.getLeaderboard(
      selectedGameId.value,
      selectedType.value,
      1,
      page.value * pageSize.value + 1
    )
    if (response.data.code === 200) {
      const currentUserId = Number(authStore.userId)
      const raw = (response.data.data || []) as Array<EntryRow & { rankPosition?: number }>
      entries.value = raw.map((entry, index) => ({
        ...entry,
        rankPosition: entry.rankPosition || index + 1,
        isMe: Boolean(currentUserId && Number(entry.userId) === currentUserId)
      }))
    } else {
      entries.value = []
      errorMessage.value = getApiBusinessMessage(response, '获取排行榜失败')
    }
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '获取排行榜失败')
    entries.value = []
  } finally {
    loading.value = false
  }
}

const fetchMyRank = async () => {
  if (selectedGameId.value == null || !authStore.isUser) {
    myRank.value = null
    return
  }
  try {
    const response = await leaderboardApi.getMyRank(selectedGameId.value, selectedType.value)
    if (response.data.code === 200 && response.data.data) {
      const d = response.data.data as { rankPosition: number; score: number }
      myRank.value = d
    } else {
      myRank.value = null
    }
  } catch (error) {
    console.error(getApiErrorMessage(error, '请求失败'), error)
    myRank.value = null
  }
}

const onGameChange = (id: number) => {
  selectedGameId.value = id
  page.value = 1
  fetchData()
}

const onTypeChange = () => {
  page.value = 1
  fetchData()
}

const changePage = (newPage: number) => {
  if (newPage < 1) return
  page.value = newPage
  fetchData()
}

const fetchData = async () => {
  await Promise.all([fetchLeaderboard(), fetchMyRank()])
}

onMounted(async () => {
  await fetchGames()
  if (selectedGameId.value != null) {
    fetchData()
  }
})
</script>

<style scoped>
.leaderboard {
  max-width: 840px;
  margin: 0 auto;
  padding: 24px;
}

:deep(.lb-avatar) {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

:deep(.lb-ph) {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-tertiary);
  border-radius: 50%;
}
</style>
