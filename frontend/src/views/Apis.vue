<template>
  <div class="apis">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>接口管理</h1>
      <div class="header-actions">
        <el-button @click="fetchApis" :loading="loading" style="margin-right: 10px">
          <Refresh :width="'1em'" :height="'1em'" />
          刷新
        </el-button>
        <el-button type="primary" @click="handleCreate">
          <Plus :width="'1em'" :height="'1em'" />
          创建接口
        </el-button>
      </div>
    </div>

    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.name" placeholder="按接口名称搜索" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-input v-model="searchForm.path" placeholder="按接口路径搜索" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="5">
          <el-select v-model="searchForm.method" placeholder="请求方法" clearable @change="handleSearch" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.enabled" placeholder="状态" clearable @change="handleSearch" style="width: 100%">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 接口列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="apiList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="接口名称" min-width="150" />
        <el-table-column prop="path" label="接口路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.method)">
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestType" label="请求类型" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.requestType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseDelay" label="延迟(ms)" width="100" align="center" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleResponses(row)">响应管理</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="接口名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入接口名称" />
        </el-form-item>
        <el-form-item label="接口路径" prop="path">
          <el-input v-model="form.path" placeholder="例如: /api/user/login" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="请求方法" prop="method">
          <el-select v-model="form.method" placeholder="请选择请求方法" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求类型" prop="requestType">
          <el-select v-model="form.requestType" placeholder="请选择请求类型" style="width: 100%">
            <el-option label="HTTP" value="HTTP" />
            <el-option label="WEBSOCKET" value="WEBSOCKET" />
          </el-select>
        </el-form-item>
        <el-form-item label="接口描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入接口描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="响应延迟(ms)" prop="responseDelay">
              <el-input-number v-model="form.responseDelay" :min="0" :max="5000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用随机返回" prop="enableRandom">
              <el-switch v-model="form.enableRandom" active-text="是" inactive-text="否" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useRoute } from 'vue-router'

const route = useRoute()

// 搜索表单
const searchForm = reactive({
  name: '',
  path: '',
  method: '',
  enabled: null
})

// 表格数据
const loading = ref(false)
const apiList = ref([])

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('创建接口')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()

// 表单数据
const form = reactive({
  id: null,
  name: '',
  path: '',
  method: 'GET',
  requestType: 'HTTP',
  description: '',
  enabled: true,
  responseDelay: 0,
  enableRandom: false
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入接口名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入接口路径', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在 1 到 200 个字符', trigger: 'blur' },
    { pattern: /^\/[a-zA-Z0-9/_-]*$/, message: '路径格式不正确，必须以/开头', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择请求方法', trigger: 'change' }
  ],
  requestType: [
    { required: true, message: '请选择请求类型', trigger: 'change' }
  ]
}

// 获取请求方法标签类型
const getMethodTagType = (method) => {
  const types = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'info'
  }
  return types[method] || 'info'
}

// 获取接口列表
const fetchApis = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize,
      ...searchForm
    }

    // 如果有projectId参数，调用项目相关的接口
    const projectId = route.query.projectId
    let response
    if (projectId) {
      response = await request({
        url: `/mock-apis/project/${projectId}`,
        method: 'get',
        params
      })
    } else {
      response = await request({
        url: '/mock-apis',
        method: 'get',
        params
      })
    }

    if (response.code === 200) {
      apiList.value = response.data
      pagination.total = response.data.length
    } else {
      ElMessage.error('获取接口列表失败')
    }
  } catch (error) {
    console.error('获取接口列表失败:', error)
    ElMessage.error('获取接口列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchApis()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.path = ''
  searchForm.method = ''
  searchForm.enabled = null
  handleSearch()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchApis()
}

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.current = page
  fetchApis()
}

// 创建接口
const handleCreate = () => {
  dialogTitle.value = '创建接口'
  isEdit.value = false
  form.id = null
  form.name = ''
  form.path = ''
  form.method = 'GET'
  form.requestType = 'HTTP'
  form.description = ''
  form.enabled = true
  form.responseDelay = 0
  form.enableRandom = false
  dialogVisible.value = true
}

// 编辑接口
const handleEdit = (row) => {
  dialogTitle.value = '编辑接口'
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.path = row.path
  form.method = row.method
  form.requestType = row.requestType
  form.description = row.description || ''
  form.enabled = row.enabled
  form.responseDelay = row.responseDelay || 0
  form.enableRandom = row.enableRandom || false
  dialogVisible.value = true
}

// 管理响应
const handleResponses = (row) => {
  ElMessage.info('响应管理功能开发中...')
}

// 删除接口
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除接口 ' + row.name + ' 吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await request({
      url: `/mock-apis/${row.id}`,
      method: 'delete'
    })
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchApis()
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
          url: '/mock-apis',
          method: 'put',
          data: form
        })
      : await request({
          url: '/mock-apis',
          method: 'post',
          data: form
        })

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
      dialogVisible.value = false
      fetchApis()
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

// 页面加载时获取数据
onMounted(() => {
  fetchApis()
})
</script>

<style scoped>
.apis {
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

.header-actions {
  display: flex;
  align-items: center;
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
</style>

