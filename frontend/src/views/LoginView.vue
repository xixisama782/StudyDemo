<template>
  <div class="login-page">
    <n-card class="login-card" title="用户登录" size="large">
      <template #header-extra>
        <n-button quaternary size="small" title="双击进入管理端" @dblclick="goToAdminLogin">
          管理端
        </n-button>
      </template>
      <n-alert v-if="error" type="error" :show-icon="false" style="margin-bottom: 16px">
        {{ error }}
      </n-alert>
      <n-form :show-label="true" @submit.prevent="handleLogin">
        <n-form-item label="用户名或邮箱">
          <n-input
            v-model:value="form.username"
            placeholder="用户名或邮箱"
            autocomplete="username"
          />
        </n-form-item>
        <n-form-item label="密码">
          <n-input
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            placeholder="密码"
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </n-form-item>
        <n-button type="primary" block :loading="loading" attr-type="submit"> 登录 </n-button>
      </n-form>
      <div class="footer-link">
        还没有账户？
        <router-link to="/register">注册</router-link>
      </div>
    </n-card>
  </div>
</template>

/** 用户登录页：提交凭证并写入 auth store，跳转 /app */
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NAlert, NButton, NCard, NForm, NFormItem, NInput } from 'naive-ui'
import { useAuthStore } from '../store/auth'
import api from '../api'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)
const error = ref('')
const router = useRouter()
const authStore = useAuthStore()

/** 双击「管理端」入口跳转管理员登录 */
const goToAdminLogin = () => {
  router.push('/admin/login')
}

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await api.post('/auth/login', form.value)
    if (response.data.code === 200) {
      authStore.setAuth({
        token: response.data.data.token,
        user: response.data.data.user,
        role: 'user'
      })
      router.push('/app')
    } else {
      error.value = getApiBusinessMessage(response, '登录失败')
    }
  } catch (err) {
    error.value = getApiErrorMessage(err, '网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  padding: 80px 16px;
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.footer-link {
  margin-top: 16px;
  font-size: 14px;
  color: var(--color-text-secondary);
  text-align: center;
}
</style>
