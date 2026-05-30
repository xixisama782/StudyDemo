<template>
  <div class="profile-page">
    <n-h2 prefix="bar" style="margin-bottom: 20px">编辑资料</n-h2>

    <n-card>
      <n-flex :wrap="true" :gap="20" align="center">
        <n-space vertical align="center">
          <n-avatar round :size="88" :src="(userInfo.avatarUrl as string) || undefined">
            {{ avatarLetter }}
          </n-avatar>
          <input
            ref="avatarInputRef"
            type="file"
            class="hide-input"
            accept="image/jpeg,image/jpg,image/png,image/webp"
            :disabled="avatarUploading"
            @change="handleAvatarSelected"
          />
          <n-button size="small" quaternary :loading="avatarUploading" @click="triggerAvatarPick">
            上传头像
          </n-button>
        </n-space>

        <div class="grow">
          <n-form :show-label="true" label-placement="left" label-width="96">
            <n-form-item label="用户名">
              <n-input :value="String(userInfo.username || '')" disabled />
            </n-form-item>
            <n-form-item label="邮箱">
              <n-input :value="String(userInfo.email || '')" disabled />
            </n-form-item>
            <n-form-item label="显示名称">
              <n-input v-model:value="editForm.displayName" placeholder="请输入显示名称" />
            </n-form-item>
          </n-form>
          <n-alert v-if="avatarSuccess" type="success" :show-icon="false" style="margin-top: 8px">
            {{ avatarSuccess }}
          </n-alert>
          <n-alert v-if="avatarError" type="error" :show-icon="false" style="margin-top: 8px">
            {{ avatarError }}
          </n-alert>
          <n-alert v-if="editError" type="error" :show-icon="false" style="margin-top: 8px">
            {{ editError }}
          </n-alert>
          <n-space style="margin-top: 16px">
            <n-button @click="router.back()">返回</n-button>
            <n-button type="primary" :loading="editLoading" @click="handleProfileUpdate">保存资料</n-button>
          </n-space>
        </div>
      </n-flex>
    </n-card>
  </div>
</template>

/** 编辑资料：显示名称与头像上传 */
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NAlert,
  NAvatar,
  NButton,
  NCard,
  NFlex,
  NForm,
  NFormItem,
  NH2,
  NInput,
  NSpace
} from 'naive-ui'
import { useAuthStore, type UserInfo } from '../store/auth'
import { userApi } from '../api'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

const router = useRouter()
const authStore = useAuthStore()
const userInfo = ref<Record<string, unknown>>({})
const editForm = reactive({ displayName: '' })
const editLoading = ref(false)
const editError = ref('')
const avatarInputRef = ref<HTMLInputElement | null>(null)
const avatarUploading = ref(false)
const avatarError = ref('')
const avatarSuccess = ref('')

const avatarLetter = computed(() => {
  const u = userInfo.value.username
  if (typeof u === 'string' && u) return u.charAt(0).toUpperCase()
  return 'U'
})

const syncAuthUser = (user: UserInfo) => {
  if (!user || !authStore.user) return
  authStore.updateUser({
    ...authStore.user,
    id: (user.id as number) ?? authStore.user.id,
    username: (user.username as string) ?? authStore.user.username,
    email: user.email ?? authStore.user.email,
    displayName: user.displayName ?? authStore.user.displayName,
    avatarUrl: user.avatarUrl ?? authStore.user.avatarUrl
  })
}

const loadProfile = async () => {
  try {
    const res = await userApi.getMe()
    if (res.data.code === 200) {
      userInfo.value = (res.data.data || {}) as Record<string, unknown>
      editForm.displayName =
        (userInfo.value.displayName as string) || (userInfo.value.username as string) || ''
      syncAuthUser(userInfo.value as UserInfo)
    } else {
      editError.value = getApiBusinessMessage(res, '获取用户资料失败')
    }
  } catch (error) {
    editError.value = getApiErrorMessage(error, '获取用户资料失败')
  }
}

const handleProfileUpdate = async () => {
  editError.value = ''
  if (!editForm.displayName.trim()) {
    editError.value = '显示名称不能为空'
    return
  }
  editLoading.value = true
  try {
    const res = await userApi.updateProfile({ displayName: editForm.displayName.trim() })
    if (res.data.code === 200) {
      userInfo.value = { ...userInfo.value, ...(res.data.data || {}) }
      syncAuthUser(userInfo.value as UserInfo)
      router.push('/app/profile')
    } else {
      editError.value = getApiBusinessMessage(res, '更新失败')
    }
  } catch (error) {
    editError.value = getApiErrorMessage(error, '更新失败')
  } finally {
    editLoading.value = false
  }
}

const triggerAvatarPick = () => {
  avatarInputRef.value?.click()
}

/** 校验格式与大小后上传头像 */
const handleAvatarSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const f = input.files?.[0]
  avatarError.value = ''
  avatarSuccess.value = ''
  if (!f) return
  if (!['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(f.type)) {
    avatarError.value = '仅支持 jpg、jpeg、png、webp 格式'
    return
  }
  if (f.size > 5 * 1024 * 1024) {
    avatarError.value = '头像大小不能超过 5MB'
    return
  }
  avatarUploading.value = true
  try {
    const res = await userApi.uploadAvatar(f)
    if (res.data.code === 200) {
      const avatarUrl = res.data.data?.avatarUrl
      userInfo.value = { ...userInfo.value, avatarUrl }
      syncAuthUser(userInfo.value as UserInfo)
      avatarSuccess.value = '头像上传成功'
    } else {
      avatarError.value = getApiBusinessMessage(res, '头像上传失败')
    }
  } catch (error) {
    avatarError.value = getApiErrorMessage(error, '头像上传失败')
  } finally {
    avatarUploading.value = false
    input.value = ''
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  max-width: 820px;
  margin: 0 auto;
  padding: 24px;
}

.grow {
  flex: 1;
  min-width: 240px;
}

.hide-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}
</style>
