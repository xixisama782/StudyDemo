<template>
  <div class="games">
    <n-h2 style="margin-bottom: 16px">选择游戏</n-h2>
    <n-space vertical :size="16">
      <n-space :wrap="true" :size="8">
        <n-button :type="selectedTypeId === null ? 'primary' : 'default'" size="small" @click="handleTypeChange(null)">
          全部
        </n-button>
        <n-button
          v-for="t in gameTypes"
          :key="t.id"
          :type="selectedTypeId === t.id ? 'primary' : 'default'"
          size="small"
          @click="handleTypeChange(t.id)"
        >
          {{ t.name }}
        </n-button>
      </n-space>
      <n-input
        v-model:value="searchKeyword"
        clearable
        placeholder="搜索游戏名称..."
        @update:value="onSearchDebounced"
      />
      <n-spin :show="loading">
        <n-empty v-if="!loading && games.length === 0" description="暂无游戏" />
        <n-grid v-else cols="1 s:2 m:3 l:4" responsive="screen" :x-gap="16" :y-gap="16">
          <n-gi v-for="game in games" :key="game.id">
            <n-card hoverable size="small" class="game-card">
              <div class="game-cover" @click="playGame(game)">
                <img v-if="game.thumbnailUrl" :src="game.thumbnailUrl" :alt="game.name" />
                <div v-else class="no-cover">
                  <n-icon :size="48">
                    <game-controller-outline />
                  </n-icon>
                </div>
              </div>
              <div class="game-meta">
                <n-text strong class="game-title" @click="playGame(game)">{{ game.name }}</n-text>
                <n-text depth="3" class="game-desc" @click="playGame(game)">{{ game.description }}</n-text>
                <n-flex justify="space-between" align="center">
                  <n-tag v-if="game.typeName" size="small" round>{{ game.typeName }}</n-tag>
                  <n-button quaternary circle size="small" @click.stop="toggleFavorite(game.id)">
                    <n-icon :size="16">
                      <heart v-if="favoriteStatus[game.id]" />
                      <heart-outline v-else />
                    </n-icon>
                  </n-button>
                </n-flex>
              </div>
            </n-card>
          </n-gi>
        </n-grid>
      </n-spin>
    </n-space>
  </div>
</template>

/** 游戏大厅：按类型/关键词筛选列表，支持收藏与进入详情 */
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NCard,
  NEmpty,
  NFlex,
  NGi,
  NGrid,
  NH2,
  NIcon,
  NInput,
  NSpin,
  NSpace,
  NTag,
  NText
} from 'naive-ui'
import { GameControllerOutline, Heart, HeartOutline } from '@vicons/ionicons5'
import { gameApi, gameTypeApi, favoriteApi } from '../api'
import { getApiErrorMessage } from '../utils/apiError'

interface GameTypeRow {
  id: number
  name: string
}

interface GameRow {
  id: number
  name: string
  description?: string
  thumbnailUrl?: string
  typeName?: string
}

const router = useRouter()
const games = ref<GameRow[]>([])
const gameTypes = ref<GameTypeRow[]>([])
const selectedTypeId = ref<number | null>(null)
const searchKeyword = ref('')
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const favoriteStatus = ref<Record<number, boolean>>({})
let searchTimer: ReturnType<typeof setTimeout> | null = null

const fetchGameTypes = async () => {
  try {
    const response = await gameTypeApi.getGameTypes()
    if (response.data.code === 200) {
      gameTypes.value = (response.data.data || []) as GameTypeRow[]
    }
  } catch (error) {
    console.error('获取游戏类型失败:', getApiErrorMessage(error, '请求失败'), error)
  }
}

const fetchGames = async (typeId: number | null = null, keyword = '') => {
  loading.value = true
  try {
    const response = await gameApi.getGameList(typeId, keyword, page.value, pageSize.value)
    if (response.data.code === 200) {
      games.value = (response.data.data.list || []) as GameRow[]
      await checkAllFavorites()
    }
  } catch (error) {
    console.error('获取游戏列表失败:', error)
    games.value = []
  } finally {
    loading.value = false
  }
}

/** 批量查询当前页游戏的收藏状态 */
const checkAllFavorites = async () => {
  const ids = games.value.map((g) => g.id)
  if (ids.length === 0) return
  try {
    const response = await favoriteApi.checkFavoritesBatch(ids)
    if (response.data.code === 200) {
      const favorited = new Set(response.data.data.favoritedGameIds || [])
      const next = { ...favoriteStatus.value }
      for (const id of ids) {
        next[id] = favorited.has(id)
      }
      favoriteStatus.value = next
    }
  } catch (error) {
    console.error('批量检查收藏失败:', getApiErrorMessage(error, '请求失败'), error)
  }
}

const toggleFavorite = async (gameId: number) => {
  try {
    if (favoriteStatus.value[gameId]) {
      await favoriteApi.removeFavorite(gameId)
      favoriteStatus.value = { ...favoriteStatus.value, [gameId]: false }
    } else {
      await favoriteApi.addFavorite(gameId)
      favoriteStatus.value = { ...favoriteStatus.value, [gameId]: true }
    }
  } catch (error) {
    console.error('切换收藏状态失败:', getApiErrorMessage(error, '请求失败'), error)
  }
}

const handleTypeChange = (typeId: number | null) => {
  selectedTypeId.value = typeId
  fetchGames(typeId, searchKeyword.value)
}

const onSearchDebounced = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchGames(selectedTypeId.value, searchKeyword.value)
  }, 300)
}

const playGame = (game: GameRow) => {
  router.push(`/app/game/${game.id}`)
}

onMounted(() => {
  fetchGameTypes()
  fetchGames()
})
</script>

<style scoped>
.games {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.game-cover {
  width: 100%;
  height: 140px;
  background: var(--color-bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
  border-radius: var(--n-border-radius);
  margin-bottom: 12px;
}

.game-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-cover {
  font-size: 48px;
  opacity: 0.5;
}

.game-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.game-title {
  cursor: pointer;
  color: var(--color-primary);
}

.game-desc {
  cursor: pointer;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
