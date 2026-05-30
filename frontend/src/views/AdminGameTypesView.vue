<template>
  <div class="admin-game-types">
    <n-h2 style="margin-bottom: 16px">游戏类型管理</n-h2>
    <n-space vertical :size="16">
      <n-button type="primary" @click="openCreateDialog">新增类型</n-button>
      <n-alert v-if="notice" type="info" :show-icon="false">{{ notice }}</n-alert>
      <n-data-table :columns="columns" :data="gameTypes" :bordered="true" size="small" />
    </n-space>

    <n-modal v-model:show="showDialog" preset="dialog" :title="dialogMode === 'create' ? '新增游戏类型' : '编辑游戏类型'">
      <n-form label-placement="top">
        <n-form-item label="名称 *">
          <n-input v-model:value="formData.name" />
        </n-form-item>
        <n-form-item label="代码 *">
          <n-input v-model:value="formData.code" :disabled="dialogMode === 'edit'" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="formData.description" type="textarea" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button quaternary @click="showDialog = false">取消</n-button>
        <n-button type="primary" @click="handleSubmit">提交</n-button>
      </template>
    </n-modal>
  </div>
</template>

/** 管理端游戏类型 CRUD */
<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import {
  NAlert,
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NH2,
  NInput,
  NModal,
  NPopconfirm,
  NSpace,
  type DataTableColumns
} from 'naive-ui'
import { gameTypeApi } from '../api'
import { getApiBusinessMessage, getApiErrorMessage } from '../utils/apiError'

interface GameTypeRow {
  id: number
  name: string
  code: string
  description?: string
}

const gameTypes = ref<GameTypeRow[]>([])
const showDialog = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const notice = ref('')
const formData = ref({
  id: null as number | null,
  name: '',
  code: '',
  description: ''
})

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
  formData.value = { id: null, name: '', code: '', description: '' }
  showDialog.value = true
}

const openEditDialog = (type: GameTypeRow) => {
  dialogMode.value = 'edit'
  formData.value = {
    id: type.id,
    name: type.name,
    code: type.code,
    description: type.description ?? ''
  }
  showDialog.value = true
}

const handleSubmit = async () => {
  try {
    let response
    if (dialogMode.value === 'create') {
      response = await gameTypeApi.createGameType(formData.value)
    } else {
      response = await gameTypeApi.updateGameType(formData.value.id!, formData.value)
    }

    if (response.data.code === 200) {
      showDialog.value = false
      notice.value = dialogMode.value === 'create' ? '游戏类型创建成功' : '游戏类型更新成功'
      fetchGameTypes()
    } else {
      notice.value = getApiBusinessMessage(response, '操作失败')
    }
  } catch (error) {
    notice.value = getApiErrorMessage(error, '操作失败')
    console.error(error)
  }
}

const handleDelete = async (id: number) => {
  try {
    const response = await gameTypeApi.deleteGameType(id)
    if (response.data.code === 200) {
      notice.value = '游戏类型删除成功'
      fetchGameTypes()
    } else {
      notice.value = getApiBusinessMessage(response, '删除失败')
    }
  } catch (error) {
    notice.value = getApiErrorMessage(error, '删除失败')
    console.error(error)
  }
}

const columns: DataTableColumns<GameTypeRow> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name' },
  { title: '代码', key: 'code' },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 180,
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
              default: () => '确定删除该游戏类型？'
            }
          )
        ]
      })
  }
]

onMounted(() => {
  fetchGameTypes()
})
</script>

<style scoped>
.admin-game-types {
  padding: 0;
}
</style>
