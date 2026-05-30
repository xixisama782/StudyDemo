<template>
  <n-layout has-sider class="admin-layout" position="absolute" style="height: 100vh">
    <n-layout-sider
      bordered
      show-trigger
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :native-scrollbar="false"
      content-style="padding: 16px 8px"
    >
      <div class="logo">
        <n-icon class="logo-icon" :size="22">
          <game-controller-outline />
        </n-icon>
        <span class="logo-text">管理后台</span>
      </div>
      <n-menu :value="activePath" :options="menuOptions" @update:value="handleMenuSelect" />
    </n-layout-sider>
    <n-layout>
      <n-layout-header embedded bordered style="padding: 0 24px; height: 64px; display: flex; align-items: center">
        <n-flex justify="space-between" align="center" style="width: 100%">
          <n-text strong>GameCenter 管理后台</n-text>
          <n-space align="center" :size="16">
            <n-text depth="3">欢迎，{{ adminName }}</n-text>
            <n-button quaternary type="error" size="small" @click="handleLogout">退出登录</n-button>
          </n-space>
        </n-flex>
      </n-layout-header>
      <n-layout-content content-style="padding: 24px" :native-scrollbar="false">
        <router-view v-slot="{ Component }">
          <transition name="gc-route" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

/** 管理后台布局：侧栏导航、顶栏与嵌套路由出口 */
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NButton,
  NFlex,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
  NIcon,
  NSpace,
  NText
} from 'naive-ui'
import { GameControllerOutline } from '@vicons/ionicons5'
import type { MenuOption } from 'naive-ui'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const adminName = ref('admin')

const activePath = computed(() => route.path)

const menuOptions: MenuOption[] = [
  { label: '仪表盘', key: '/dashboard' },
  { label: '游戏管理', key: '/games' },
  { label: '类型管理', key: '/game-types' },
  { label: '用户管理', key: '/users' }
]

onMounted(() => {
  const admin = authStore.admin
  if (admin && typeof admin.displayName === 'string' && admin.displayName) {
    adminName.value = admin.displayName
  } else if (admin && typeof admin.username === 'string' && admin.username) {
    adminName.value = admin.username
  }
})

const handleMenuSelect = (key: string) => {
  router.push(key)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  background-color: var(--color-bg-primary);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px 20px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.logo-icon {
  color: var(--n-text-color);
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
}
</style>
