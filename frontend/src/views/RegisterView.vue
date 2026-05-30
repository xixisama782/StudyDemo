<template>

  <div class="register-page">

    <n-card title="用户注册" size="large" class="register-card">

      <n-alert v-if="error" type="error" :show-icon="false" style="margin-bottom: 16px">

        {{ error }}

      </n-alert>

      <n-form @submit.prevent="handleRegister">

        <n-form-item label="用户名">

          <n-input v-model:value="form.username" placeholder="用户名" autocomplete="username" />

        </n-form-item>

        <n-form-item label="邮箱">

          <n-input-group>

            <n-input

              v-model:value="form.email"

              type="text"

              placeholder="用于接收验证码"

              autocomplete="email"

            />

            <n-button

              type="primary"

              ghost

              attr-type="button"

              :disabled="!canSendCode || codeSending || codeCountdown > 0"

              :loading="codeSending"

              @click.prevent.stop="handleSendCode"

            >

              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}

            </n-button>

          </n-input-group>

        </n-form-item>

        <n-form-item label="邮箱验证码">

          <n-input v-model:value="form.verificationCode" placeholder="6 位验证码" maxlength="6" />

        </n-form-item>

        <n-form-item label="密码">

          <n-input

            v-model:value="form.password"

            type="password"

            show-password-on="click"

            placeholder="密码"

            autocomplete="new-password"

          />

        </n-form-item>

        <n-button type="primary" block :loading="loading" attr-type="submit">

          {{ loading ? '注册中...' : '注册' }}

        </n-button>

      </n-form>

      <div class="footer-link">

        已有账户？

        <router-link to="/login">登录</router-link>

      </div>

    </n-card>

  </div>

</template>



/** 用户注册页：邮箱验证码 + 注册表单 */
<script setup lang="ts">

import { computed, onUnmounted, ref } from 'vue'

import { useRouter } from 'vue-router'

import { NAlert, NButton, NCard, NForm, NFormItem, NInput, NInputGroup, useMessage } from 'naive-ui'

import api, { authApi } from '../api'

import { getApiBusinessMessage, getApiErrorMessage, isApiSuccess } from '../utils/apiError'



const form = ref({

  username: '',

  password: '',

  email: '',

  verificationCode: ''

})

const loading = ref(false)

const error = ref('')

const codeSending = ref(false)

const codeCountdown = ref(0)

const router = useRouter()

const message = useMessage()

let countdownTimer: ReturnType<typeof setInterval> | null = null



const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/



const canSendCode = computed(() => emailPattern.test(form.value.email.trim()))



const startCountdown = () => {

  codeCountdown.value = 60

  if (countdownTimer) clearInterval(countdownTimer)

  countdownTimer = setInterval(() => {

    codeCountdown.value -= 1

    if (codeCountdown.value <= 0 && countdownTimer) {

      clearInterval(countdownTimer)

      countdownTimer = null

    }

  }, 1000)

}



/** 发送注册验证码（60s 倒计时防重复） */
const handleSendCode = async () => {

  if (!canSendCode.value || codeCountdown.value > 0 || codeSending.value) return

  codeSending.value = true

  error.value = ''

  try {

    const response = await authApi.sendEmailCode(form.value.email.trim(), 'register')

    if (isApiSuccess(response)) {

      startCountdown()

      message.success(getApiBusinessMessage(response, '验证码已发送'))

    } else if (codeCountdown.value === 0) {

      error.value = getApiBusinessMessage(response, '验证码发送失败')

    }

  } catch (err) {

    if (codeCountdown.value === 0) {

      error.value = getApiErrorMessage(err, '验证码发送失败')

    }

  } finally {

    codeSending.value = false

  }

}



/** 提交注册，成功后跳转登录页 */
const handleRegister = async () => {

  if (!form.value.verificationCode.trim()) {

    error.value = '请填写邮箱验证码'

    return

  }

  loading.value = true

  error.value = ''

  try {

    const response = await api.post('/auth/register', {

      username: form.value.username,

      password: form.value.password,

      email: form.value.email.trim(),

      verificationCode: form.value.verificationCode.trim()

    })

    if (isApiSuccess(response)) {

      router.push('/login')

    } else {

      error.value = getApiBusinessMessage(response, '注册失败')

    }

  } catch (err) {

    error.value = getApiErrorMessage(err, '网络错误')

  } finally {

    loading.value = false

  }

}



onUnmounted(() => {

  if (countdownTimer) clearInterval(countdownTimer)

})

</script>



<style scoped>

.register-page {

  display: flex;

  justify-content: center;

  padding: 48px 16px;

}



.register-card {

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

