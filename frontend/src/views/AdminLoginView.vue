<template>
  <div class="admin-login-page">
    <n-card title="管理员登录" size="large" class="login-card">
      <n-alert v-if="error" type="error" :show-icon="false" style="margin-bottom: 16px">
        {{ error }}
      </n-alert>
      <n-form @submit.prevent="handleLogin">
        <n-form-item label="管理员用户名">
          <n-input v-model:value="form.username" placeholder="用户名" autocomplete="username" />
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
        <n-button type="error" block :loading="loading" attr-type="submit">
          {{ loading ? '登录中...' : '管理员登录' }}
        </n-button>
      </n-form>
      <div class="footer-link">
        返回用户登录：
        <router-link to="/login">用户登录</router-link>
      </div>
    </n-card>
  </div>
</template>

/** 管理员登录页 */
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

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await api.post('/admin/auth/login', form.value)
    if (response.data.code === 200) {
      authStore.setAuth({
        token: response.data.data.token,
        admin: response.data.data.admin,
        role: 'admin'
      })
      router.push('/admin')
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
.admin-login-page {
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
