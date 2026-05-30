<template>
  <div class="user-app">
    <header class="app-header">
      <n-text strong style="font-size: 16px">欢迎，{{ user?.username || '用户' }}</n-text>
      <n-dropdown trigger="click" :options="gearOptions" @select="handleGearAction">
        <n-button quaternary circle class="gear-btn">
          <n-icon :size="18">
            <settings-outline />
          </n-icon>
        </n-button>
      </n-dropdown>
    </header>
    <main class="app-content">
      <router-view v-slot="{ Component }">
        <transition name="gc-route" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <nav class="bottom-nav">
      <n-space justify="space-around" style="width: 100%; padding: 8px 0">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          v-slot="{ navigate, href }"
          :to="item.to"
          custom
        >
          <n-button
            class="nav-item-btn"
            :quaternary="!isActive(item.names)"
            :type="isActive(item.names) ? 'primary' : 'default'"
            round
            tag="a"
            :href="href"
            @click="(e: MouseEvent) => { e.preventDefault(); navigate() }"
          >
            <span class="nav-btn-inner">
              <n-icon class="nav-icon" :size="18">
                <component :is="item.icon" />
              </n-icon>
              <span class="nav-label">{{ item.label }}</span>
            </span>
          </n-button>
        </router-link>
      </n-space>
    </nav>

    <n-modal v-model:show="showLogoutConfirm" preset="dialog" title="退出登录">
      <div>确定要退出登录吗？</div>
      <template #action>
        <n-button quaternary @click="showLogoutConfirm = false">取消</n-button>
        <n-button type="error" @click="confirmLogout">确认退出</n-button>
      </template>
    </n-modal>
  </div>
</template>

/** 用户端壳层：顶栏、底部 Tab 导航与子路由出口 */
<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NDropdown, NIcon, NModal, NSpace, NText } from 'naive-ui'
import { GameControllerOutline, PersonOutline, SettingsOutline, StarOutline, TimeOutline } from '@vicons/ionicons5'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const user = computed(() => authStore.user)
const showLogoutConfirm = ref(false)

interface NavItem {
  to: string
  names: readonly string[]
  label: string
  icon: Component
}

const navItems: NavItem[] = [
  { to: '/app', names: ['Games', 'GameLobby', 'GamePlay'] as const, label: '游戏', icon: GameControllerOutline },
  { to: '/app/history', names: ['History'] as const, label: '历史', icon: TimeOutline },
  { to: '/app/favorites', names: ['Favorites'] as const, label: '收藏', icon: StarOutline },
  { to: '/app/profile', names: ['Profile', 'ProfileEdit', 'AccountSettings'] as const, label: '我的', icon: PersonOutline }
]

const isActive = (names: readonly string[]) => {
  const n = route.name
  if (typeof n !== 'string') return false
  return names.includes(n)
}

const gearOptions = computed(() => [{ key: 'logout', label: '退出登录' }])

const handleGearAction = (key: string | number) => {
  if (key === 'logout') {
    showLogoutConfirm.value = true
  }
}

const confirmLogout = async () => {
  showLogoutConfirm.value = false
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.user-app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--color-bg-primary);
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(20, 24, 28, 0.72);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--border-color);
  transition:
    background var(--gc-duration-fast, 0.16s) var(--gc-ease-out, ease),
    border-color var(--gc-duration-fast, 0.16s) var(--gc-ease-out, ease);
}

.gear-btn {
  font-size: 18px;
}

.bottom-nav {
  background: var(--color-bg-secondary);
  border-top: 1px solid var(--border-color);
  padding-bottom: env(safe-area-inset-bottom, 0);
  transition: border-color var(--gc-duration-fast, 0.16s) var(--gc-ease-out, ease);
}

@media (prefers-reduced-motion: no-preference) {
  :deep(.nav-item-btn) {
    transition:
      transform var(--gc-duration-fast, 0.16s) var(--gc-ease-out, ease),
      filter var(--gc-duration-fast, 0.16s) var(--gc-ease-out, ease);
  }

  :deep(.nav-item-btn:active) {
    transform: scale(0.96);
  }
}

.nav-btn-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.nav-icon {
  line-height: 1;
}

.nav-label {
  font-size: 11px;
  font-weight: 500;
}

.app-content {
  flex: 1;
  overflow-y: auto;
  background: var(--color-bg-primary);
}
</style>
