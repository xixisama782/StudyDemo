<template>
  <div class="favorites">
    <n-h2 style="margin-bottom: 16px">我的收藏</n-h2>
    <n-spin :show="loading">
      <n-empty v-if="!loading && favorites.length === 0" description="暂无收藏游戏">
        <template #extra>
          <n-button type="primary" @click="goToGames">去逛逛</n-button>
        </template>
      </n-empty>
      <n-list v-else bordered>
        <n-list-item v-for="item in favorites" :key="item.id">
          <n-thing>
            <template #avatar>
              <div class="cover" @click="playGame(item)">
                <img v-if="item.thumbnailUrl" :src="item.thumbnailUrl" :alt="item.gameName" />
                <span v-else class="placeholder">
                  <n-icon :size="28">
                    <game-controller-outline />
                  </n-icon>
                </span>
              </div>
            </template>
            <template #header>
              <n-text strong style="cursor: pointer" @click="playGame(item)">{{ item.gameName }}</n-text>
            </template>
            <template #description>
              <n-text v-if="item.typeName" depth="3" tag="div">{{ item.typeName }}</n-text>
              <n-text depth="3" tag="div">收藏于 {{ formatDate(item.createdAt) }}</n-text>
            </template>
            <template #action>
              <n-space>
                <n-button size="small" type="primary" @click="playGame(item)">开始游戏</n-button>
                <n-button size="small" @click="confirmRemove(item.gameId)">取消收藏</n-button>
              </n-space>
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

/** 我的收藏：分页列表、取消收藏与跳转游戏大厅 */
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NEmpty,
  NH2,
  NIcon,
  NList,
  NListItem,
  NSpin,
  NSpace,
  NText,
  NThing,
  useDialog
} from 'naive-ui'
import { GameControllerOutline } from '@vicons/ionicons5'
import { favoriteApi } from '../api'
import { getApiErrorMessage } from '../utils/apiError'

interface FavoriteRow {
  id: number
  gameId: number
  gameName: string
  thumbnailUrl?: string
  typeName?: string
  createdAt?: string
}

const router = useRouter()
const dialog = useDialog()
const favorites = ref<FavoriteRow[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const fetchFavorites = async () => {
  loading.value = true
  try {
    const response = await favoriteApi.getFavorites(page.value, pageSize.value)
    if (response.data.code === 200) {
      favorites.value = (response.data.data.list || []) as FavoriteRow[]
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取收藏列表失败:', getApiErrorMessage(error, '请求失败'), error)
  } finally {
    loading.value = false
  }
}

const changePage = (newPage: number) => {
  page.value = newPage
  fetchFavorites()
}

const confirmRemove = (gameId: number) => {
  dialog.warning({
    title: '取消收藏',
    content: '确定取消收藏该游戏吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const response = await favoriteApi.removeFavorite(gameId)
        if (response.data.code === 200) {
          await fetchFavorites()
        }
      } catch (error) {
        console.error('取消收藏失败:', getApiErrorMessage(error, '请求失败'), error)
      }
    }
  })
}

const playGame = (item: FavoriteRow) => {
  if (item.gameId) {
    router.push(`/app/game/${item.gameId}`)
  }
}

const goToGames = () => {
  router.push('/app')
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
  fetchFavorites()
})
</script>

<style scoped>
.favorites {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.cover {
  width: 96px;
  height: 72px;
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
  font-size: 28px;
  opacity: 0.5;
}
</style>
