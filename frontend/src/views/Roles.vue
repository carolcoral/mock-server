<template>
  <div class="roles">
    <div class="page-header">
      <h1>{{ $t('permission.role.title') }}</h1>
      <el-button type="primary" @click="handleCreate" v-if="isAdmin">
        <Plus />
        {{ $t('permission.role.createRole') }}
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="roleList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" :label="$t('permission.role.roleName')" min-width="120" />
        <el-table-column prop="code" :label="$t('permission.role.roleCode')" min-width="150" />
        <el-table-column prop="description" :label="$t('permission.role.description')" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('permission.role.isDefault')" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">{{ $t('permission.role.defaultTag') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('permission.role.createdAt')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('permission.role.actions')" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" :disabled="!isAdmin">{{ $t('permission.role.edit') }}</el-button>
            <el-button
              v-if="!row.isDefault"
              type="success"
              link
              @click="handleSetDefault(row)"
              :disabled="!isAdmin"
            >{{ $t('permission.role.setDefault') }}</el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="!isAdmin || row.code === 'ROLE_ADMIN'"
            >{{ $t('permission.role.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑角色对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item :label="$t('permission.role.roleName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('permission.role.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('permission.role.roleCode')" prop="code">
          <el-input v-model="form.code" :placeholder="$t('permission.role.codePlaceholder')" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('permission.role.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="$t('permission.role.descriptionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('permission.role.isDefault')">
          <el-switch v-model="form.isDefault" />
          <span class="form-hint">{{ $t('permission.role.isDefaultHint') }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatTime, loadDateFormat } from '@/utils/dateFormat'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)

const loading = ref(false)
const roleList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  isDefault: false
})

const rules = computed(() => ({
  name: [
    { required: true, message: t('permission.role.nameRequired'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: t('permission.role.codeRequired'), trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]*$/, message: t('permission.role.codeFormat'), trigger: 'blur' }
  ]
}))

const fetchRoles = async () => {
  loading.value = true
  try {
    const response = await request.get('/roles')
    if (response.code === 200) {
      roleList.value = response.data || []
    } else {
      ElMessage.error(t('permission.role.fetchFailed'))
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error(t('permission.role.fetchFailed'))
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = t('permission.role.createRole')
  isEdit.value = false
  form.id = null
  form.name = ''
  form.code = ''
  form.description = ''
  form.isDefault = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = t('permission.role.editRole')
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.code = row.code
  form.description = row.description || ''
  form.isDefault = row.isDefault || false
  dialogVisible.value = true
}

const handleSetDefault = async (row) => {
  try {
    await ElMessageBox.confirm(
      t('permission.role.isDefaultHint'),
      t('common.info'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'info' }
    )
    const response = await request.put(`/roles/${row.id}/set-default`)
    if (response.code === 200) {
      ElMessage.success(t('permission.role.setDefaultSuccess'))
      fetchRoles()
    } else {
      ElMessage.error(response.message || t('permission.role.setDefaultFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置默认角色失败:', error)
      ElMessage.error(t('permission.role.setDefaultFailed'))
    }
  }
}

const handleDelete = async (row) => {
  if (row.code === 'ROLE_ADMIN') {
    ElMessage.error(t('permission.role.cannotDeleteAdmin'))
    return
  }

  try {
    await ElMessageBox.confirm(
      t('permission.role.confirmDelete', { name: row.name }),
      t('common.warning'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )
    const response = await request.delete(`/roles/${row.id}`)
    if (response.code === 200) {
      ElMessage.success(t('permission.role.deleteSuccess'))
      fetchRoles()
    } else {
      ElMessage.error(response.message || t('permission.role.deleteFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error(t('permission.role.deleteFailed'))
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const submitData = {
      name: form.name,
      code: form.code,
      description: form.description || null,
      isDefault: form.isDefault
    }

    const response = isEdit.value
      ? await request.put(`/roles/${form.id}`, submitData)
      : await request.post('/roles', submitData)

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? t('permission.role.editSuccess') : t('permission.role.createSuccess'))
      dialogVisible.value = false
      fetchRoles()
    } else {
      ElMessage.error(response.message || (isEdit.value ? t('permission.role.editFailed') : t('permission.role.createFailed')))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? t('permission.role.editFailed') : t('permission.role.createFailed'))
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(async () => {
  await loadDateFormat()
  fetchRoles()
})
</script>

<style scoped>
.roles {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.table-card {
  margin-bottom: 20px;
}

.form-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
