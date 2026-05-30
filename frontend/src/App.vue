<template>
  <n-config-provider
    :locale="zhCN"
    :date-locale="dateZhCN"
    :theme="darkTheme"
    :theme-overrides="themeOverrides"
  >
    <n-global-style />
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view v-slot="{ Component }">
            <transition name="gc-route" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<!-- 根组件：全局 Naive UI 主题、消息/对话框提供者及路由出口 -->
<script setup lang="ts">
import {
  darkTheme,
  zhCN,
  dateZhCN,
  NConfigProvider,
  NDialogProvider,
  NGlobalStyle,
  NMessageProvider,
  NNotificationProvider
} from 'naive-ui'
import type { GlobalThemeOverrides } from 'naive-ui'

/** 暗色主题与设计 token 覆盖 */
const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#7C3AED',
    primaryColorHover: '#8b5cf6',
    primaryColorPressed: '#6d28d9',
    primaryColorSuppl: '#9d5ff5',
    bodyColor: '#0A0C0F',
    cardColor: '#14181C',
    modalColor: '#14181C',
    popoverColor: '#14181C',
    tableColor: '#14181C',
    inputColor: '#1E2329',
    actionColor: '#14181C',
    borderColor: 'rgba(255, 255, 255, 0.08)',
    hoverColor: 'rgba(255, 255, 255, 0.06)',
    pressedColor: 'rgba(255, 255, 255, 0.08)',
    borderRadius: '12px',
    borderRadiusSmall: '8px',
    cubicBezierEaseInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
    cubicBezierEaseOut: 'cubic-bezier(0.22, 1, 0.36, 1)',
    cubicBezierEaseIn: 'cubic-bezier(0.4, 0, 1, 1)',
    boxShadow1: '0 4px 24px rgba(0, 0, 0, 0.35)',
    boxShadow2: '0 12px 48px rgba(0, 0, 0, 0.45)',
    scrollbarColor: 'rgba(255, 255, 255, 0.12)',
    scrollbarColorHover: 'rgba(124, 58, 237, 0.35)'
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --color-bg-primary: #0A0C0F;
  --color-bg-secondary: #14181C;
  --color-bg-tertiary: #1E2329;
  --color-primary: #7C3AED;
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-error: #EF4444;
  --color-text-primary: #FFFFFF;
  --color-text-secondary: #9CA3AF;
  --color-text-tertiary: #6B7280;
  --border-color: rgba(255, 255, 255, 0.08);
  /* 动效 token：与 Naive themeOverrides 中的 cubicBezier 一致，便于自定义 CSS */
  --gc-duration: 0.24s;
  --gc-duration-fast: 0.16s;
  --gc-ease-out: cubic-bezier(0.22, 1, 0.36, 1);
  --gc-ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--color-bg-primary);
  color: var(--color-text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
}

@media (prefers-reduced-motion: no-preference) {
  html {
    scroll-behavior: smooth;
  }
}

/* 路由级切换：轻微位移动画 + 淡入淡出，模式 out-in 避免父子视图叠层闪烁 */
.gc-route-enter-active,
.gc-route-leave-active {
  transition:
    opacity var(--gc-duration) var(--gc-ease-out),
    transform var(--gc-duration) var(--gc-ease-out);
}

.gc-route-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.gc-route-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (prefers-reduced-motion: reduce) {
  .gc-route-enter-active,
  .gc-route-leave-active {
    transition-duration: 0.01ms !important;
    transition-property: opacity !important;
  }

  .gc-route-enter-from,
  .gc-route-leave-to {
    transform: none !important;
  }

  html {
    scroll-behavior: auto;
  }
}

a {
  color: var(--color-primary);
  text-decoration: none;
}

a:hover {
  color: #9d5ff5;
}

input,
select,
textarea,
button {
  font-family: inherit;
}
</style>
