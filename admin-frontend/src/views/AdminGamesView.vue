<template>
  <div class="admin-games">
    <n-h2 style="margin-bottom: 16px">游戏管理</n-h2>
    <n-space vertical :size="16">
      <n-space :wrap="true" :size="8">
        <n-input v-model:value="filters.keyword" placeholder="搜索游戏名称" clearable style="width: 220px" @keyup.enter="handleSearch" />
        <n-select
          v-model:value="filters.typeId"
          placeholder="全部类型"
          clearable
          style="width: 180px"
          :options="typeOptions"
          @update:value="handleSearch"
        />
        <n-button @click="handleSearch">筛选</n-button>
        <n-button quaternary @click="resetFilters">重置</n-button>
        <n-button type="primary" @click="openCreateDialog">新增游戏</n-button>
      </n-space>
      <n-alert v-if="notice" type="info" :show-icon="false">{{ notice }}</n-alert>
      <n-data-table :columns="columns" :data="games" :bordered="true" size="small" :scroll-x="980" />
      <n-space justify="end" align="center">
        <n-text depth="3">共 {{ total }} 条</n-text>
        <n-button size="small" :disabled="page === 1" @click="handlePageChange(page - 1)">上一页</n-button>
        <n-text depth="3">{{ page }} / {{ totalPages }}</n-text>
        <n-button size="small" :disabled="page * pageSize >= total" @click="handlePageChange(page + 1)">
          下一页
        </n-button>
      </n-space>
    </n-space>

    <n-modal
      v-model:show="showDialog"
      preset="dialog"
      :title="dialogMode === 'create' ? '新增游戏' : '编辑游戏'"
      style="width: 560px; max-width: 95vw"
    >
      <n-form label-placement="top" :show-require-mark="false">
        <n-form-item label="游戏名称 *">
          <n-input v-model:value="formData.name" placeholder="名称" />
        </n-form-item>
        <n-form-item label="游戏类型 *">
          <n-select v-model:value="formData.typeId" :options="typeOptionsForForm" placeholder="请选择" />
        </n-form-item>
        <n-form-item label="游戏简介">
          <n-input v-model:value="formData.description" type="textarea" placeholder="简介" />
        </n-form-item>
        <n-form-item label="资源地址 *">
          <n-input v-model:value="formData.resourceUrl" placeholder="resourceUrl" />
        </n-form-item>
        <n-form-item label="封面图地址">
          <n-input v-model:value="formData.thumbnailUrl" placeholder="thumbnailUrl" />
        </n-form-item>
        <n-form-item label="提供者">
          <n-input v-model:value="formData.provider" />
        </n-form-item>
        <n-form-item label="标签">
          <n-input v-model:value="formData.tags" placeholder="逗号分隔" />
        </n-form-item>
        <n-form-item label="上架">
          <n-switch v-model:value="formData.isActive" />
        </n-form-item>
        <n-alert v-if="formError" type="error" :show-icon="false">{{ formError }}</n-alert>
      </n-form>
      <template #action>
        <n-button quaternary @click="showDialog = false">取消</n-button>
        <n-button type="primary" @click="handleSubmit">提交</n-button>
      </template>
    </n-modal>
  </div>
</template>

/** 管理端游戏 CRUD：筛选、分页与上架状态 */
<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import {
  NAlert,
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NH2,
  NInput,
  NModal,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  NText,
  NPopconfirm,
  type DataTableColumns
} from 'naive-ui'
import { adminGameApi, gameTypeApi } from '../api'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

interface GameTypeRow {
  id: number
  name: string
}

interface AdminGameRow {
  id: number
  name: string
  description?: string
  typeId?: number
  typeName?: string
  isActive?: boolean
  playCount?: number
  thumbnailUrl?: string
  resourceUrl?: string
  provider?: string
  tags?: string
}

const games = ref<AdminGameRow[]>([])
const gameTypes = ref<GameTypeRow[]>([])
const showDialog = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formData = ref({
  id: null as number | null,
  name: '',
  description: '',
  typeId: null as number | null,
  resourceUrl: '',
  thumbnailUrl: '',
  provider: '',
  tags: '',
  isActive: true
})
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filters = ref<{ keyword: string; typeId: number | null }>({ keyword: '', typeId: null })
const formError = ref('')
const notice = ref('')
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const typeOptions = computed(() =>
  gameTypes.value.map((t) => ({
    label: t.name,
    value: t.id
  }))
)

const typeOptionsForForm = typeOptions

const fetchGames = async () => {
  try {
    const typeId = filters.value.typeId != null ? filters.value.typeId : null
    const keyword = filters.value.keyword.trim() || null
    const response = await adminGameApi.getGameList(typeId, keyword, page.value, pageSize.value)
    if (response.data.code === 200) {
      games.value = (response.data.data.list || []) as AdminGameRow[]
      total.value = response.data.data.total || 0
    } else {
      notice.value = getApiBusinessMessage(response, '获取游戏列表失败')
    }
  } catch (error) {
    notice.value = getApiErrorMessage(error, '获取游戏列表失败')
    console.error(error)
  }
}

const fetchGameTypes = async () => {
  try {
    const response = await gameTypeApi.getGameTypes()
    if (response.data.code === 200) {
      gameTypes.value = (response.data.data || []) as GameTypeRow[]
    }
  } catch (error) {
    console.error(getApiErrorMessage(error, '请求失败'), error)
  }
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  formError.value = ''
  formData.value = {
    id: null,
    name: '',
    description: '',
    typeId: null,
    resourceUrl: '',
    thumbnailUrl: '',
    provider: '',
    tags: '',
    isActive: true
  }
  showDialog.value = true
}

const openEditDialog = (game: AdminGameRow) => {
  dialogMode.value = 'edit'
  formError.value = ''
  formData.value = {
    id: game.id,
    name: game.name,
    description: game.description || '',
    typeId: game.typeId ?? null,
    resourceUrl: game.resourceUrl || '',
    thumbnailUrl: game.thumbnailUrl || '',
    provider: game.provider || '',
    tags: game.tags || '',
    isActive: game.isActive !== false
  }
  showDialog.value = true
}

const validateForm = () => {
  if (!formData.value.name?.trim()) return '请输入游戏名称'
  if (formData.value.typeId == null) return '请选择游戏类型'
  if (!formData.value.resourceUrl?.trim()) return '请输入资源地址'
  return ''
}

const handleSubmit = async () => {
  formError.value = validateForm()
  if (formError.value) return
  try {
    const data = {
      ...formData.value,
      name: formData.value.name.trim(),
      resourceUrl: formData.value.resourceUrl.trim(),
      thumbnailUrl: formData.value.thumbnailUrl?.trim() || '',
      typeId: formData.value.typeId!
    }

    let response
    if (dialogMode.value === 'create') {
      response = await adminGameApi.createGame(data)
    } else {
      response = await adminGameApi.updateGame(formData.value.id!, data)
    }

    if (response.data.code === 200) {
      showDialog.value = false
      notice.value = dialogMode.value === 'create' ? '游戏创建成功' : '游戏更新成功'
      fetchGames()
    } else {
      formError.value = getApiBusinessMessage(response, '操作失败')
    }
  } catch (error) {
    formError.value = getApiErrorMessage(error, '操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    const response = await adminGameApi.deleteGame(id)
    if (response.data.code === 200) {
      notice.value = '游戏删除成功'
      if (games.value.length === 1 && page.value > 1) {
        page.value -= 1
      }
      fetchGames()
    } else {
      notice.value = getApiBusinessMessage(response, '删除失败')
    }
  } catch (error) {
    notice.value = getApiErrorMessage(error, '删除失败')
    console.error(error)
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  fetchGames()
}

const handleSearch = () => {
  page.value = 1
  fetchGames()
}

const resetFilters = () => {
  filters.value = { keyword: '', typeId: null }
  page.value = 1
  fetchGames()
}

const columns: DataTableColumns<AdminGameRow> = [
  { title: 'ID', key: 'id', width: 70 },
  {
    title: '封面',
    key: 'thumbnailUrl',
    width: 80,
    render: (row) =>
      row.thumbnailUrl
        ? h('img', { src: row.thumbnailUrl, style: { width: '48px', height: '48px', objectFit: 'cover', borderRadius: '4px' } })
        : '无'
  },
  { title: '名称', key: 'name', ellipsis: { tooltip: true } },
  { title: '类型', key: 'typeName' },
  {
    title: '状态',
    key: 'isActive',
    render: (row) =>
      h(
        NTag,
        { type: row.isActive ? 'success' : 'default', size: 'small' },
        { default: () => (row.isActive ? '上架' : '下架') }
      )
  },
  { title: '游玩次数', key: 'playCount' },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'small', onClick: () => openEditDialog(row) }, { default: () => '编辑' }),
          h(
            NPopconfirm,
            {
              onPositiveClick: () => {
                void handleDelete(row.id)
              }
            },
            {
              trigger: () =>
                h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
              default: () => '确定删除该游戏？'
            }
          )
        ]
      })
  }
]

onMounted(() => {
  fetchGames()
  fetchGameTypes()
})
</script>

<style scoped>
.admin-games {
  padding: 0;
}
</style>
