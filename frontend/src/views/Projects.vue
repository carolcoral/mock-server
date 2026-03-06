<template>
  <div class="projects">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>项目管理</h1>
      <el-button type="primary" @click="handleCreate">
        <Plus :width="'1em'" :height="'1em'" />
        创建项目
      </el-button>
    </div>

    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input v-model="searchForm.name" placeholder="按项目名称搜索" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-input v-model="searchForm.code" placeholder="按项目编码搜索" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.enabled" placeholder="状态" clearable @change="handleSearch" style="width: 100%">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 项目列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="projectList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" min-width="150" />
        <el-table-column prop="code" label="项目编码" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)" :disabled="!canEditProject(row)">编辑</el-button>
            <el-button type="primary" link @click="$router.push(`/apis?projectId=${row.id}`)">接口管理</el-button>
            <el-button type="success" link @click="handleManageMembers(row)" :disabled="!canManageMembers(row)">成员管理</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="!canDeleteProject(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入项目编码（唯一）" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 成员管理对话框 -->
    <el-dialog v-model="memberDialogVisible" title="项目成员管理" width="50%" @close="handleMemberDialogClose">
      <div class="member-header">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-button type="primary" @click="handleAddMember">
              <Plus :width="'1em'" :height="'1em'" />
              添加成员
            </el-button>
          </el-col>
          <el-col :span="12" style="text-align: right;">
            <span style="color: #909399; font-size: 14px;">
              <span>当前项目：</span>
              <span style="color: #303133; font-weight: 600;">{{ currentProject?.name }}</span>
            </span>
          </el-col>
        </el-row>
      </div>

      <!-- 成员列表 -->
      <el-table
        v-loading="memberLoading"
        :data="memberList"
        border
        style="width: 100%; margin-top: 16px;"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="id" label="成员ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="{ row }">
            <span>{{ row.username || `用户${row.userId}` }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">
            <span>{{ row.email || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'CREATOR'" type="danger" size="small">创建者</el-tag>
            <el-tag v-else-if="row.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
            <el-tag v-else type="info" size="small">普通成员</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="加入时间" width="180" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'CREATOR'"
              type="primary"
              link
              @click="handleUpdateMemberRole(row)"
            >
              修改角色
            </el-button>
            <el-button
              v-if="row.role !== 'CREATOR'"
              type="danger"
              link
              @click="handleRemoveMember(row)"
            >
              移除
            </el-button>
            <span v-else style="color: #909399; font-size: 12px;">不可操作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑成员对话框 -->
    <el-dialog v-model="addMemberDialogVisible" :title="addMemberDialogTitle" width="500px" style="max-width: 50%;" @close="handleAddMemberDialogClose">
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="80px">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="memberForm.userId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入用户名搜索"
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
        <el-form-item label="角色" prop="role">
          <el-select v-model="memberForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通成员" value="MEMBER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="memberSubmitLoading" @click="handleMemberSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  enabled: null
})

// 表格数据
const loading = ref(false)
const projectList = ref([])

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('创建项目')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()

// 表单数据
const form = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  enabled: true
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线和连字符', trigger: 'blur' }
  ]
}

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    // 管理员查看所有项目，普通用户只查看有权限的项目
    const url = isAdmin.value ? '/projects' : '/projects/accessible'

    const response = await request({
      url,
      method: 'get'
    })
    if (response.code === 200) {
      // 客户端过滤
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
      ElMessage.error('获取项目列表失败')
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchProjects()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.enabled = null
  handleSearch()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchProjects()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.current = page
  fetchProjects()
}

// 创建项目
const handleCreate = () => {
  dialogTitle.value = '创建项目'
  isEdit.value = false
  form.id = null
  form.name = ''
  form.code = ''
  form.description = ''
  form.enabled = true
  dialogVisible.value = true
}

// 编辑项目
const handleEdit = (row) => {
  dialogTitle.value = '编辑项目'
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.code = row.code
  form.description = row.description
  form.enabled = row.enabled
  dialogVisible.value = true
}

// 删除项目
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除项目 ' + row.name + ' 吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await request({
      url: `/projects/${row.id}`,
      method: 'delete'
    })
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchProjects()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
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
      ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
      dialogVisible.value = false
      fetchProjects()
    } else {
      ElMessage.error(response.message || (isEdit.value ? '编辑失败' : '创建失败'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '编辑失败' : '创建失败')
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// ====== 成员管理相关 ======
const memberDialogVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const currentProject = ref(null)

const addMemberDialogVisible = ref(false)
const addMemberDialogTitle = ref('添加成员')
const memberFormRef = ref()
const memberSubmitLoading = ref(false)
const userSearchLoading = ref(false)
const userOptions = ref([])

const memberForm = reactive({
  userId: null,
  role: 'MEMBER'
})

const memberRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 打开成员管理对话框
const handleManageMembers = async (row) => {
  currentProject.value = row
  memberDialogVisible.value = true
  await fetchProjectMembers(row.id)
}

// 搜索用户
const searchUsers = async (query) => {
  if (!query) {
    userOptions.value = []
    return
  }

  userSearchLoading.value = true
  try {
    const response = await request({
      url: '/users',
      method: 'get'
    })
    if (response.code === 200) {
      const users = response.data || []
      userOptions.value = users.filter(user =>
        user.username.toLowerCase().includes(query.toLowerCase()) ||
        user.email.toLowerCase().includes(query.toLowerCase())
      )
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

// 获取项目成员列表
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
      ElMessage.error('获取成员列表失败')
    }
  } catch (error) {
    console.error('获取成员列表失败:', error)
    ElMessage.error('获取成员列表失败')
  } finally {
    memberLoading.value = false
  }
}

// 打开添加成员对话框
const handleAddMember = () => {
  addMemberDialogTitle.value = '添加成员'
  memberForm.userId = null
  memberForm.role = 'MEMBER'
  userOptions.value = []
  addMemberDialogVisible.value = true
}

// 提交成员添加
const handleMemberSubmit = async () => {
  try {
    await memberFormRef.value.validate()
    memberSubmitLoading.value = true

    // 添加项目成员
    const addResponse = await request({
      url: `/project-members/${currentProject.value.id}/users/${memberForm.userId}?role=${memberForm.role}`,
      method: 'post'
    })

    if (addResponse.code === 200) {
      ElMessage.success('添加成功')
      addMemberDialogVisible.value = false
      fetchProjectMembers(currentProject.value.id)
    } else {
      ElMessage.error(addResponse.message || '添加失败')
    }
  } catch (error) {
    console.error('添加成员失败:', error)
    ElMessage.error('添加成员失败')
  } finally {
    memberSubmitLoading.value = false
  }
}

// 修改成员角色
const handleUpdateMemberRole = async (member) => {
  try {
    const userName = member.username || `用户${member.userId}`

    const { value } = await ElMessageBox.prompt(
      '请选择新的角色',
      `修改 ${userName} 的角色`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^(ADMIN|MEMBER)$/,
        inputErrorMessage: '请输入 ADMIN 或 MEMBER',
        inputValue: member.role === 'ADMIN' ? 'ADMIN' : 'MEMBER'
      }
    )

    const response = await request({
      url: `/project-members/${currentProject.value.id}/users/${member.userId}`,
      method: 'put',
      params: { role: value }
    })

    if (response.code === 200) {
      ElMessage.success('修改成功')
      fetchProjectMembers(currentProject.value.id)
    } else {
      ElMessage.error('修改失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改角色失败:', error)
      ElMessage.error('修改角色失败')
    }
  }
}

// 移除成员
const handleRemoveMember = async (member) => {
  try {
    const userName = member.username || `用户${member.userId}`

    await ElMessageBox.confirm(
      `确认将 ${userName} 从项目中移除吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await request({
      url: `/project-members/${currentProject.value.id}/users/${member.userId}`,
      method: 'delete'
    })

    if (response.code === 200) {
      ElMessage.success('移除成功')
      fetchProjectMembers(currentProject.value.id)
    } else {
      ElMessage.error('移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('移除成员失败')
    }
  }
}

// 关闭成员管理对话框
const handleMemberDialogClose = () => {
  memberList.value = []
  currentProject.value = null
}

// 关闭添加成员对话框
const handleAddMemberDialogClose = () => {
  memberFormRef.value?.resetFields()
}

// ====== 权限判断方法 ======

// 判断是否可以编辑项目（创建者或管理员）
const canEditProject = (project) => {
  if (isAdmin.value) return true
  const role = project.userRole
  return role === 'CREATOR' || role === 'ADMIN'
}

// 判断是否可以管理成员（创建者或管理员）
const canManageMembers = (project) => {
  if (isAdmin.value) return true
  const role = project.userRole
  return role === 'CREATOR' || role === 'ADMIN'
}

// 判断是否可以删除项目（只有创建者可以删除）
const canDeleteProject = (project) => {
  if (isAdmin.value) return true
  const role = project.userRole
  return role === 'CREATOR'
}

// 页面加载时获取数据
onMounted(() => {
  fetchProjects()
})

// 页面加载时获取数据
onMounted(() => {
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

