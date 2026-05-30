<template>

  <div class="settings-page">

    <n-h2 prefix="bar" style="margin-bottom: 20px">账户设置</n-h2>



    <n-card title="修改密码">

      <n-tabs v-model:value="passwordMode" type="line" animated>

        <n-tab-pane name="email" tab="邮箱验证码">

          <n-form :show-label="true" label-placement="left" label-width="96">

            <n-form-item v-if="userEmail" label="绑定邮箱">

              <n-text>{{ userEmail }}</n-text>

            </n-form-item>

            <n-alert v-else type="warning" :show-icon="false" style="margin-bottom: 12px">

              当前账户未绑定邮箱，请使用「原密码验证」或先在个人资料中绑定邮箱。

            </n-alert>

            <n-form-item label="验证码">

              <n-input-group>

                <n-input

                  v-model:value="emailForm.verificationCode"

                  placeholder="6 位验证码"

                  maxlength="6"

                  :disabled="!userEmail"

                />

                <n-button

                  type="primary"

                  ghost

                  :disabled="!userEmail || codeCountdown > 0"

                  :loading="codeSending"

                  @click="handleSendPasswordCode"

                >

                  {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}

                </n-button>

              </n-input-group>

            </n-form-item>

            <n-form-item label="新密码">

              <n-input

                v-model:value="emailForm.newPassword"

                type="password"

                show-password-on="click"

                placeholder="至少 6 位"

              />

            </n-form-item>

            <n-form-item label="确认密码">

              <n-input

                v-model:value="emailForm.confirmPassword"

                type="password"

                show-password-on="click"

                placeholder="再次输入新密码"

              />

            </n-form-item>

          </n-form>

        </n-tab-pane>

        <n-tab-pane name="old" tab="原密码验证">

          <n-form :show-label="true" label-placement="left" label-width="96">

            <n-form-item label="旧密码">

              <n-input

                v-model:value="passwordForm.oldPassword"

                type="password"

                show-password-on="click"

                placeholder="请输入旧密码"

              />

            </n-form-item>

            <n-form-item label="新密码">

              <n-input

                v-model:value="passwordForm.newPassword"

                type="password"

                show-password-on="click"

                placeholder="至少 6 位"

              />

            </n-form-item>

            <n-form-item label="确认密码">

              <n-input

                v-model:value="passwordForm.confirmPassword"

                type="password"

                show-password-on="click"

                placeholder="再次输入新密码"

              />

            </n-form-item>

          </n-form>

        </n-tab-pane>

      </n-tabs>



      <n-alert v-if="passwordError" type="error" :show-icon="false" style="margin-top: 12px">

        {{ passwordError }}

      </n-alert>

      <n-alert v-if="passwordSuccess" type="success" :show-icon="false" style="margin-top: 8px">

        {{ passwordSuccess }}

      </n-alert>

      <n-space style="margin-top: 14px">

        <n-button @click="router.push('/app/profile')">返回个人中心</n-button>

        <n-button type="primary" :loading="passwordLoading" @click="handlePasswordChange">

          确认修改

        </n-button>

      </n-space>

    </n-card>

  </div>

</template>



/** 账户设置：邮箱验证码或原密码两种方式修改密码 */
<script setup lang="ts">

import { onMounted, onUnmounted, reactive, ref } from 'vue'

import { useRouter } from 'vue-router'

import {

  NAlert,

  NButton,

  NCard,

  NForm,

  NFormItem,

  NH2,

  NInput,

  NInputGroup,

  NSpace,

  NTabPane,

  NTabs,

  NText,

  useMessage

} from 'naive-ui'

import { userApi } from '../api'

import { getApiBusinessMessage, getApiErrorMessage, isApiSuccess } from '../utils/apiError'



const router = useRouter()
const message = useMessage()

const passwordMode = ref<'email' | 'old'>('email')

const userEmail = ref('')

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const emailForm = reactive({ verificationCode: '', newPassword: '', confirmPassword: '' })

const passwordLoading = ref(false)

const passwordError = ref('')

const passwordSuccess = ref('')

const codeSending = ref(false)

const codeCountdown = ref(0)

let countdownTimer: ReturnType<typeof setInterval> | null = null



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



onMounted(async () => {

  try {

    const res = await userApi.getMe()

    if (res.data.code === 200 && res.data.data) {

      const email = res.data.data.email

      userEmail.value = typeof email === 'string' ? email : ''

      if (!userEmail.value) {

        passwordMode.value = 'old'

      }

    }

  } catch {

    /* ignore */

  }

})



onUnmounted(() => {

  if (countdownTimer) clearInterval(countdownTimer)

})



const validateNewPasswords = (newPassword: string, confirmPassword: string): boolean => {

  if (!newPassword) {

    passwordError.value = '请输入新密码'

    return false

  }

  if (newPassword.length < 6) {

    passwordError.value = '新密码长度至少6位'

    return false

  }

  if (newPassword !== confirmPassword) {

    passwordError.value = '两次密码输入不一致'

    return false

  }

  return true

}



const handleSendPasswordCode = async () => {

  if (!userEmail.value || codeCountdown.value > 0) return

  codeSending.value = true

  passwordError.value = ''

  try {

    const res = await userApi.sendPasswordChangeCode()

    if (isApiSuccess(res)) {

      startCountdown()

      message.success(getApiBusinessMessage(res, '验证码已发送至绑定邮箱'))

    } else {

      passwordError.value = getApiBusinessMessage(res, '验证码发送失败')

    }

  } catch (error) {

    passwordError.value = getApiErrorMessage(error, '验证码发送失败')

  } finally {

    codeSending.value = false

  }

}



/** 按当前 Tab 提交对应改密 payload */
const handlePasswordChange = async () => {

  passwordError.value = ''

  passwordSuccess.value = ''



  if (passwordMode.value === 'email') {

    if (!emailForm.verificationCode.trim()) {

      passwordError.value = '请填写邮箱验证码'

      return

    }

    if (!validateNewPasswords(emailForm.newPassword, emailForm.confirmPassword)) return



    passwordLoading.value = true

    try {

      const res = await userApi.changePassword({

        verificationCode: emailForm.verificationCode.trim(),

        newPassword: emailForm.newPassword

      })

      if (res.data.code === 200) {

        passwordSuccess.value = '密码修改成功'

        emailForm.verificationCode = ''

        emailForm.newPassword = ''

        emailForm.confirmPassword = ''

      } else {

        passwordError.value = getApiBusinessMessage(res, '修改失败')

      }

    } catch (error) {

      passwordError.value = getApiErrorMessage(error, '修改失败')

    } finally {

      passwordLoading.value = false

    }

    return

  }



  if (!passwordForm.oldPassword) {

    passwordError.value = '请输入旧密码'

    return

  }

  if (!validateNewPasswords(passwordForm.newPassword, passwordForm.confirmPassword)) return



  passwordLoading.value = true

  try {

    const res = await userApi.changePassword({

      oldPassword: passwordForm.oldPassword,

      newPassword: passwordForm.newPassword

    })

    if (res.data.code === 200) {

      passwordSuccess.value = '密码修改成功'

      passwordForm.oldPassword = ''

      passwordForm.newPassword = ''

      passwordForm.confirmPassword = ''

    } else {

      passwordError.value = getApiBusinessMessage(res, '修改失败')

    }

  } catch (error) {

    passwordError.value = getApiErrorMessage(error, '修改失败')

  } finally {

    passwordLoading.value = false

  }

}

</script>



<style scoped>

.settings-page {

  max-width: 820px;

  margin: 0 auto;

  padding: 24px;

}

</style>

