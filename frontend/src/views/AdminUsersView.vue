<template>
  <div class="admin-users">
    <n-h2 prefix="bar" style="margin-bottom: 16px">用户管理</n-h2>
    <n-space vertical :size="16">
      <n-alert v-if="notice" type="info" :show-icon="false">{{ notice }}</n-alert>
      <n-space :wrap="true" :size="8">
        <n-input
          v-model:value="searchForm.keyword"
          placeholder="搜索用户名/邮箱"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />
        <n-select
          v-model:value="searchForm.status"
          placeholder="全部状态"
          clearable
          style="width: 140px"
          :options="statusOptions"
          @update:value="handleSearch"
        />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
      </n-space>

      <n-spin :show="loading">
        <n-data-table :columns="columns" :data="users" :bordered="true" size="small" />
      </n-spin>

      <n-space v-if="total > 0" justify="center" align="center">
        <n-text depth="3">共 {{ total }} 条</n-text>
        <n-button size="small" :disabled="page <= 1" @click="changePage(page - 1)">上一页</n-button>
        <n-text depth="3">{{ page }} / {{ totalPages }}</n-text>
        <n-button size="small" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</n-button>
      </n-space>
    </n-space>

    <n-modal v-model:show="showDetailModal" preset="dialog" title="用户详情" style="width: 520px">
      <n-descriptions bordered :column="1" size="small" label-placement="left">
        <n-descriptions-item label="用户名">{{ currentUser.username }}</n-descriptions-item>
        <n-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</n-descriptions-item>
        <n-descriptions-item label="显示名">{{ currentUser.displayName || '-' }}</n-descriptions-item>
        <n-descriptions-item label="状态">
          <n-tag :type="currentUser.status === 'normal' ? 'success' : 'error'" size="small">
            {{ currentUser.status === 'normal' ? '正常' : '禁用' }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="注册时间">{{ formatDate(currentUser.createdAt as string) }}</n-descriptions-item>
        <n-descriptions-item label="更新时间">{{ formatDate(currentUser.updatedAt as string) }}</n-descriptions-item>
        <n-descriptions-item label="总游玩次数">{{ currentUser.playCount ?? 0 }}</n-descriptions-item>
        <n-descriptions-item label="收藏游戏数">{{ currentUser.favoriteCount ?? 0 }}</n-descriptions-item>
      </n-descriptions>
      <template #action>
        <n-button type="primary" @click="showResetFromDetail">重置密码</n-button>
        <n-button quaternary @click="closeDetailModal">关闭</n-button>
      </template>
    </n-modal>

    <n-modal v-model:show="showResetModal" preset="dialog" title="重置密码">
      <n-text depth="3">用户: {{ currentUser.username }}</n-text>
      <n-form label-placement="top" style="margin-top: 12px">
        <n-form-item label="新密码">
          <n-input v-model:value="resetForm.newPassword" type="password" show-password-on="click" />
        </n-form-item>
        <n-form-item label="确认密码">
          <n-input v-model:value="resetForm.confirmPassword" type="password" show-password-on="click" />
        </n-form-item>
        <n-alert v-if="resetError" type="error" :show-icon="false">{{ resetError }}</n-alert>
      </n-form>
      <template #action>
        <n-button quaternary @click="closeResetModal">取消</n-button>
        <n-button type="primary" :loading="resetLoading" @click="confirmReset">确认重置</n-button>
      </template>
    </n-modal>
  </div>
</template>

/** 管理端用户：搜索、详情、启用/禁用与重置密码 */
<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import {
  NAlert,
  NButton,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NForm,
  NFormItem,
  NH2,
  NInput,
  NModal,
  NSelect,
  NSpin,
  NSpace,
  NTag,
  NText,
  useDialog,
  type DataTableColumns
} from 'naive-ui'
import { adminUserApi } from '../api'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

interface AdminUserRow {
  id: number
  username: string
  email?: string
  displayName?: string
  status: string
  createdAt?: string
}

const users = ref<AdminUserRow[]>([])
const loading = ref(true)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const searchForm = ref<{ keyword: string; status: string | null }>({ keyword: '', status: null })
const showDetailModal = ref(false)
const showResetModal = ref(false)
const currentUser = ref<Record<string, unknown>>({})
const resetForm = ref({ newPassword: '', confirmPassword: '' })
const resetError = ref('')
const resetLoading = ref(false)
const notice = ref('')
const dialog = useDialog()

const statusOptions = [
  { label: '正常', value: 'normal' },
  { label: '禁用', value: 'disabled' }
]

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

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

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await adminUserApi.getUsers(
      searchForm.value.keyword || undefined,
      searchForm.value.status || undefined,
      page.value,
      pageSize.value
    )
    if (res.data.code === 200) {
      users.value = (res.data.data.list || []) as AdminUserRow[]
      total.value = res.data.data.total || 0
    }
  } catch (err) {
    console.error(getApiErrorMessage(err, '请求失败'), err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadUsers()
}

const changePage = (newPage: number) => {
  page.value = newPage
  loadUsers()
}

const showUserDetail = async (user: AdminUserRow) => {
  try {
    const res = await adminUserApi.getUserById(user.id)
    if (res.data.code === 200) {
      currentUser.value = (res.data.data || {}) as Record<string, unknown>
      showDetailModal.value = true
    }
  } catch (err) {
    console.error(getApiErrorMessage(err, '请求失败'), err)
  }
}

const closeDetailModal = () => {
  showDetailModal.value = false
  currentUser.value = {}
}

const showResetFromDetail = () => {
  showDetailModal.value = false
  showResetModal.value = true
  resetForm.value = { newPassword: '', confirmPassword: '' }
  resetError.value = ''
}

const closeResetModal = () => {
  showResetModal.value = false
}

const toggleUserStatus = (user: AdminUserRow) => {
  const newStatus = user.status === 'normal' ? 'disabled' : 'normal'
  const action = newStatus === 'disabled' ? '禁用' : '启用'
  dialog.warning({
    title: '确认操作',
    content: `确定要${action}用户 ${user.username} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await adminUserApi.updateUserStatus(user.id, newStatus)
        if (res.data.code === 200) {
          notice.value = `${action}成功`
          loadUsers()
        } else {
          notice.value = getApiBusinessMessage(res, `${action}失败`)
        }
      } catch (err) {
        notice.value = getApiErrorMessage(err, `${action}失败`)
      }
    }
  })
}

const confirmReset = async () => {
  resetError.value = ''
  if (!resetForm.value.newPassword) {
    resetError.value = '请输入新密码'
    return
  }
  if (resetForm.value.newPassword !== resetForm.value.confirmPassword) {
    resetError.value = '两次密码输入不一致'
    return
  }
  if (resetForm.value.newPassword.length < 6) {
    resetError.value = '密码长度不能少于6位'
    return
  }

  resetLoading.value = true
  try {
    const uid = currentUser.value.id as number
    const res = await adminUserApi.resetPassword(uid, resetForm.value.newPassword)
    if (res.data.code === 200) {
      notice.value = `用户 ${currentUser.value.username} 的密码已重置`
      closeResetModal()
    } else {
      resetError.value = getApiBusinessMessage(res, '重置失败')
    }
  } catch (err) {
    resetError.value = getApiErrorMessage(err, '重置失败')
  } finally {
    resetLoading.value = false
  }
}

const columns: DataTableColumns<AdminUserRow> = [
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户名', key: 'username' },
  { title: '邮箱', key: 'email', render: (r) => r.email || '-' },
  { title: '显示名', key: 'displayName', render: (r) => r.displayName || '-' },
  {
    title: '状态',
    key: 'status',
    render: (row) =>
      h(
        NTag,
        { type: row.status === 'normal' ? 'success' : 'error', size: 'small' },
        { default: () => (row.status === 'normal' ? '正常' : '禁用') }
      )
  },
  { title: '注册时间', key: 'createdAt', render: (r) => formatDate(r.createdAt) },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => showUserDetail(row) }, { default: () => '详情' }),
          h(
            NButton,
            {
              size: 'small',
              type: row.status === 'normal' ? 'error' : 'success',
              secondary: true,
              onClick: () => toggleUserStatus(row)
            },
            { default: () => (row.status === 'normal' ? '禁用' : '启用') }
          )
        ]
      })
  }
]

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-users {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
