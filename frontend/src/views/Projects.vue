<template>
  <div class="projects">
    <div class="page-header">
      <h1>{{ $t('project.title') }}</h1>
      <el-button type="primary" @click="handleCreate">
        <Plus :width="'1em'" :height="'1em'" />
        {{ $t('project.createProject') }}
      </el-button>
    </div>

    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input v-model="searchForm.name" :placeholder="$t('project.searchByName')" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-input v-model="searchForm.code" :placeholder="$t('project.searchByCode')" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.enabled" :placeholder="$t('project.status')" clearable @change="handleSearch" style="width: 100%">
            <el-option :label="$t('project.enabledStatus')" :value="true" />
            <el-option :label="$t('project.disabledStatus')" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">{{ $t('project.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('project.reset') }}</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="projectList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" :label="$t('project.projectName')" min-width="150" />
        <el-table-column prop="code" :label="$t('project.projectCode')" min-width="120" />
        <el-table-column prop="description" :label="$t('project.description')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enabled" :label="$t('project.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? $t('project.enabledStatus') : $t('project.disabledStatus') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('project.createdAt')" width="200">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('project.actions')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" :disabled="!canEditProject(row)">{{ $t('project.editProject') }}</el-button>
            <el-button type="primary" link @click="$router.push(`/apis?projectId=${row.id}`)">{{ $t('project.apiManagement') }}</el-button>
            <el-button type="success" link @click="handleManageMembers(row)" :disabled="!canManageMembers(row)">{{ $t('project.memberManagement') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="!canDeleteProject(row)">{{ $t('project.deleteProject') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('project.projectName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('project.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('project.projectCode')" prop="code">
          <el-input v-model="form.code" :placeholder="$t('project.codePlaceholder')" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('project.projectDescription')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="$t('project.descriptionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('project.status')" prop="enabled">
          <el-switch v-model="form.enabled" :active-text="$t('project.enabledStatus')" :inactive-text="$t('project.disabledStatus')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="memberDialogVisible" :title="$t('project.memberTitle')" width="50%" @close="handleMemberDialogClose">
      <div class="member-header">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-button type="primary" @click="handleAddMember">
              <Plus :width="'1em'" :height="'1em'" />
              {{ $t('project.addMember') }}
            </el-button>
          </el-col>
          <el-col :span="12" style="text-align: right;">
            <span style="color: #909399; font-size: 14px;">
              <span>{{ $t('project.currentProject') }}</span>
              <span style="color: #303133; font-weight: 600;">{{ currentProject?.name }}</span>
            </span>
          </el-col>
        </el-row>
      </div>

      <el-table
        v-loading="memberLoading"
        :data="memberList"
        border
        style="width: 100%; margin-top: 16px;"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="id" :label="$t('project.memberId')" width="80" />
        <el-table-column prop="userId" :label="$t('project.userId')" width="80" />
        <el-table-column prop="username" :label="$t('project.username')" min-width="120">
          <template #default="{ row }">
            <span>{{ row.username || $t('project.username') + row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="$t('project.email')" min-width="180">
          <template #default="{ row }">
            <span>{{ row.email || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" :label="$t('project.role')" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'CREATOR'" type="danger" size="small">{{ $t('project.creator') }}</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning" size="small">{{ $t('project.admin') }}</el-tag>
            <el-tag v-else type="info" size="small">{{ $t('project.member') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('project.joinTime')" width="200">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('project.actions')" width="150" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'CREATOR'"
              type="primary"
              link
              @click="handleUpdateMemberRole(row)"
            >
              {{ $t('project.modifyRole') }}
            </el-button>
            <el-button
              v-if="row.role !== 'CREATOR'"
              type="danger"
              link
              @click="handleRemoveMember(row)"
            >
              {{ $t('project.remove') }}
            </el-button>
            <span v-else style="color: #909399; font-size: 12px;">{{ $t('project.noOperation') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="addMemberDialogVisible" :title="addMemberDialogTitle" width="500px" style="max-width: 50%;" @close="handleAddMemberDialogClose">
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="80px">
        <el-form-item :label="$t('project.selectUser')" prop="userId">
          <el-select
            v-model="memberForm.userId"
            filterable
            remote
            reserve-keyword
            :placeholder="$t('project.searchUsers')"
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.username} (${user.email})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.role')" prop="role">
          <el-select v-model="memberForm.role" :placeholder="$t('project.selectRole')" style="width: 100%;">
            <el-option :label="$t('project.admin')" value="ADMIN" />
            <el-option :label="$t('project.member')" value="MEMBER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="memberSubmitLoading" @click="handleMemberSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatTime, loadDateFormat } from '@/utils/dateFormat'

const { t } = useI18n()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const searchForm = reactive({
  name: '',
  code: '',
  enabled: null
})

const loading = ref(false)
const projectList = ref([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

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
  enabled: true
})

const rules = computed(() => ({
  name: [
    { required: true, message: t('project.nameRequired'), trigger: 'blur' },
    { min: 2, max: 100, message: t('project.nameLengthError'), trigger: 'blur' }
  ],
  code: [
    { required: true, message: t('project.codeRequired'), trigger: 'blur' },
    { min: 2, max: 50, message: t('project.codeLengthError'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: t('project.codeFormatError'), trigger: 'blur' }
  ]
}))

const fetchProjects = async () => {
  loading.value = true
  try {
    const url = isAdmin.value ? '/projects' : '/projects/accessible'

    const response = await request({
      url,
      method: 'get'
    })
    if (response.code === 200) {
      let filteredData = response.data || []

      if (searchForm.name) {
        filteredData = filteredData.filter(p =>
          p.name?.toLowerCase().includes(searchForm.name.toLowerCase())
        )
      }
      if (searchForm.code) {
        filteredData = filteredData.filter(p =>
          p.code?.toLowerCase().includes(searchForm.code.toLowerCase())
        )
      }
      if (searchForm.enabled !== null) {
        filteredData = filteredData.filter(p => p.enabled === searchForm.enabled)
      }

      projectList.value = filteredData
      pagination.total = filteredData.length
    } else {
      ElMessage.error(t('project.fetchFailed'))
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error(t('project.fetchFailed'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchProjects()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.enabled = null
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchProjects()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  fetchProjects()
}

const handleCreate = () => {
  dialogTitle.value = t('project.createProject')
  isEdit.value = false
  form.id = null
  form.name = ''
  form.code = ''
  form.description = ''
  form.enabled = true
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = t('project.editProject')
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.code = row.code
  form.description = row.description
  form.enabled = row.enabled
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(t('project.confirmDelete', { name: row.name }), t('common.info'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    const response = await request({
      url: `/projects/${row.id}`,
      method: 'delete'
    })
    if (response.code === 200) {
      ElMessage.success(t('project.deleteSuccess'))
      fetchProjects()
    } else {
      ElMessage.error(t('project.deleteFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(t('project.deleteFailed'))
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const response = isEdit.value
      ? await request({
          url: '/projects',
          method: 'put',
          data: form
        })
      : await request({
          url: '/projects',
          method: 'post',
          data: form
        })

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? t('project.editSuccess') : t('project.createSuccess'))
      dialogVisible.value = false
      fetchProjects()
    } else {
      ElMessage.error(response.message || (isEdit.value ? t('project.editFailed') : t('project.createFailed')))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? t('project.editFailed') : t('project.createFailed'))
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// ====== 成员管理相关 ======
const memberDialogVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const currentProject = ref(null)

const addMemberDialogVisible = ref(false)
const addMemberDialogTitle = ref('')
const memberFormRef = ref()
const memberSubmitLoading = ref(false)
const userSearchLoading = ref(false)
const userOptions = ref([])

const memberForm = reactive({
  userId: null,
  role: 'MEMBER'
})

const memberRules = computed(() => ({
  userId: [
    { required: true, message: t('project.selectUser'), trigger: 'change' }
  ],
  role: [
    { required: true, message: t('project.selectRole'), trigger: 'change' }
  ]
}))

const handleManageMembers = async (row) => {
  currentProject.value = row
  memberDialogVisible.value = true
  await fetchProjectMembers(row.id)
}

const searchUsers = async (query) => {
  if (!query) {
    userOptions.value = []
    return
  }

  userSearchLoading.value = true
  try {
    const response = await request({
      url: '/users/search',
      method: 'get',
      params: { keyword: query }
    })
    if (response.code === 200) {
      userOptions.value = response.data || []
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

const fetchProjectMembers = async (projectId) => {
  memberLoading.value = true
  try {
    const response = await request({
      url: `/project-members/${projectId}`,
      method: 'get'
    })
    if (response.code === 200) {
      memberList.value = response.data || []
    } else {
      ElMessage.error(t('project.fetchMembersFailed'))
    }
  } catch (error) {
    console.error('获取成员列表失败:', error)
    ElMessage.error(t('project.fetchMembersFailed'))
  } finally {
    memberLoading.value = false
  }
}

const handleAddMember = () => {
  addMemberDialogTitle.value = t('project.addMember')
  memberForm.userId = null
  memberForm.role = 'MEMBER'
  userOptions.value = []
  addMemberDialogVisible.value = true
}

const handleMemberSubmit = async () => {
  try {
    await memberFormRef.value.validate()
    memberSubmitLoading.value = true

    const addResponse = await request({
      url: `/project-members/${currentProject.value.id}/users/${memberForm.userId}?role=${memberForm.role}`,
      method: 'post'
    })

    if (addResponse.code === 200) {
      ElMessage.success(t('project.addMemberSuccess'))
      addMemberDialogVisible.value = false
      fetchProjectMembers(currentProject.value.id)
    } else {
      ElMessage.error(addResponse.message || t('project.addMemberFail'))
    }
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error(t('project.addMemberFailed'))
  } finally {
    memberSubmitLoading.value = false
  }
}

const handleUpdateMemberRole = async (member) => {
  try {
    const userName = member.username || `${t('project.username')}${member.userId}`

    const { value } = await ElMessageBox.prompt(
      t('project.modifyRolePrompt'),
      t('project.modifyRoleTitle', { name: userName }),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        inputPattern: /^(ADMIN|MEMBER)$/,
        inputErrorMessage: t('project.modifyRoleHint'),
        inputValue: member.role === 'ADMIN' ? 'ADMIN' : 'MEMBER'
      }
    )

    const response = await request({
      url: `/project-members/${currentProject.value.id}/users/${member.userId}`,
      method: 'put',
      params: { role: value }
    })

    if (response.code === 200) {
      ElMessage.success(t('project.roleUpdated'))
      fetchProjectMembers(currentProject.value.id)
    } else {
      ElMessage.error(t('project.roleUpdateFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改角色失败:', error)
      ElMessage.error(t('project.roleUpdateFailed'))
    }
  }
}

const handleRemoveMember = async (member) => {
  try {
    const userName = member.username || `${t('project.username')}${member.userId}`

    await ElMessageBox.confirm(
      t('project.confirmRemove', { name: userName }),
      t('common.info'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    const response = await request({
      url: `/project-members/${currentProject.value.id}/users/${member.userId}`,
      method: 'delete'
    })

    if (response.code === 200) {
      ElMessage.success(t('project.removeSuccess'))
      fetchProjectMembers(currentProject.value.id)
    } else {
      ElMessage.error(t('project.removeFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error(t('project.removeMemberFailed'))
    }
  }
}

const handleMemberDialogClose = () => {
  memberList.value = []
  currentProject.value = null
}

const handleAddMemberDialogClose = () => {
  memberFormRef.value?.resetFields()
}

const canEditProject = (project) => {
  if (isAdmin.value) return true
  const role = project.userRole
  return role === 'CREATOR' || role === 'ADMIN'
}

const canManageMembers = (project) => {
  if (isAdmin.value) return true
  const role = project.userRole
  return role === 'CREATOR' || role === 'ADMIN'
}

const canDeleteProject = (project) => {
  if (isAdmin.value) return true
  const role = project.userRole
  return role === 'CREATOR'
}

onMounted(async () => {
  await loadDateFormat()
  fetchProjects()
})
</script>

<style scoped>
.projects {
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.stat-card) {
  margin-bottom: 0;
}

.member-header {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
</style>

