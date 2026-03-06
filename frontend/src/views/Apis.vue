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
        <el-col :span="4">
          <el-input v-model="searchForm.name" placeholder="按接口名称搜索" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="5">
          <el-input v-model="searchForm.path" placeholder="按接口路径搜索" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.projectId" placeholder="选择项目" clearable @change="handleSearch" style="width: 100%" filterable>
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="searchForm.method" placeholder="请求方法" clearable @change="handleSearch" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="searchForm.enabled" placeholder="状态" clearable @change="handleSearch" style="width: 100%">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="5">
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
        <el-table-column prop="project.name" label="项目" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="接口名称" min-width="150" />
        <el-table-column label="接口路径" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleCopyPath(row)"
              style="font-family: monospace;"
            >
              /api/mock-server/{{ row.project?.code }}{{ row.path }}
            </el-button>
          </template>
        </el-table-column>
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
        <el-form-item label="选择项目" prop="projectId" v-if="!isEdit">
          <el-select v-model="form.projectId" placeholder="请选择项目" clearable style="width: 100%" filterable>
            <el-option
              v-for="project in accessibleProjects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="接口名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入接口名称" />
        </el-form-item>
        <el-form-item label="接口路径" prop="path">
          <el-input v-model="form.path" placeholder="例如: /api/user/login 或 /api/user/{userId}" :disabled="isEdit">
            <template #append>
              <el-tooltip content="支持RESTful风格，例如 /api/user/{userId}，其中userId为参数名" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
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
              <el-switch
                v-model="form.enableRandom"
                active-text="是"
                inactive-text="否"
                :disabled="hasMultipleActiveResponses && !form.enableRandom"
              />
              <div v-if="hasMultipleActiveResponses && !form.enableRandom" style="color: #F56C6C; font-size: 12px; margin-top: 4px;">
                该接口已激活多个响应，必须启用随机返回
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="hasMultipleActiveResponses && form.enableRandom" prop="warning">
          <div style="color: #909399; font-size: 14px; padding: 10px; background: #f4f4f5; border-left: 3px solid #409EFF; border-radius: 4px;">
            <el-icon><WarningFilled /></el-icon>
            <span style="margin-left: 8px;">已激活多个响应，关闭随机返回将无法正常使用接口</span>
          </div>
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

    <!-- 响应管理对话框 -->
    <el-dialog v-model="responseDialogVisible" :title="`响应管理 - ${currentApi?.name || ''}`" width="70%" @close="handleResponseDialogClose">
      <!-- 响应状态分组提示 -->
      <div class="response-status-tip" style="margin-bottom: 16px; padding: 12px; background: #f0f9ff; border: 1px solid #b3d8ff; border-radius: 4px;">
        <el-icon><InfoFilled /></el-icon>
        <span style="margin-left: 8px; color: #409EFF; font-size: 14px;">
          提示：选中激活状态的响应作为接口默认返回。启用随机返回时，会从所有启用且激活的响应中按权重随机选择。
        </span>
      </div>

      <div class="response-header">
        <el-row :gutter="20">
          <el-col :span="18">
            <span style="color: #909399; font-size: 14px;">
              <span>接口路径：</span>
              <span style="color: #303133; font-weight: 600;">/api/mock-server/{{ currentApi?.project?.code }}{{ currentApi?.path }}</span>
              <span style="margin-left: 20px;">方法：</span>
              <el-tag :type="getMethodTagType(currentApi?.method)">{{ currentApi?.method }}</el-tag>
            </span>
          </el-col>
          <el-col :span="6" style="text-align: right;">
            <el-button type="primary" @click="handleAddResponse">
              <Plus :width="'1em'" :height="'1em'" />
              添加响应
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 响应列表 -->
      <el-table
        v-loading="responseLoading"
        :data="responseList"
        border
        style="width: 100%; margin-top: 16px;"
        :header-cell-style="{ background: '#f5f7fa' }"
        :row-class-name="getResponseRowClass"
        row-key="id"
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="statusCode" label="状态码" width="80" />
        <el-table-column label="响应内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.responseBody }}
          </template>
        </el-table-column>
        <el-table-column prop="contentType" label="内容类型" width="130" />
        <el-table-column prop="weight" label="权重" width="70" align="center" />
        <el-table-column prop="responseDelay" label="延迟(ms)" width="80" align="center" />
        <el-table-column prop="isDefault" label="默认" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isDefault ? 'warning' : 'info'" size="small">
              {{ row.isDefault ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="70" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleUpdateResponse(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="active" label="激活" width="70" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.active"
              @change="handleToggleActive(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleEditResponse(row)"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              link
              size="small"
              @click="handleRequestParams(row)"
            >
              参数
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDeleteResponse(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑响应对话框 -->
    <el-dialog v-model="responseFormDialogVisible" :title="responseFormTitle" width="700px" @close="handleResponseFormDialogClose">
      <el-form ref="responseFormRef" :model="responseForm" :rules="responseRules" label-width="100px">
        <el-form-item label="状态码" prop="statusCode">
          <el-input-number v-model="responseForm.statusCode" :min="100" :max="599" style="width: 100%" />
        </el-form-item>
        <el-form-item label="内容类型" prop="contentType">
          <el-select v-model="responseForm.contentType" placeholder="请选择内容类型" style="width: 100%">
            <el-option label="application/json" value="application/json" />
            <el-option label="text/html" value="text/html" />
            <el-option label="text/plain" value="text/plain" />
            <el-option label="application/xml" value="application/xml" />
          </el-select>
        </el-form-item>
        <el-form-item label="响应头" prop="headers">
          <el-input v-model="responseForm.headers" type="textarea" :rows="2" placeholder='{"X-Custom-Header": "value"}' />
        </el-form-item>
        <el-form-item label="响应体" prop="responseBody">
          <el-input v-model="responseForm.responseBody" type="textarea" :rows="8" placeholder="请输入响应体内容" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权重" prop="weight">
              <el-input-number v-model="responseForm.weight" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用" prop="enabled">
              <el-switch v-model="responseForm.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="响应延迟" prop="responseDelay">
              <el-input-number v-model="responseForm.responseDelay" :min="0" :max="60000" placeholder="0" style="width: 100%">
                <template #append>ms</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否默认" prop="isDefault">
              <el-switch v-model="responseForm.isDefault" />
              <span style="margin-left: 10px; font-size: 12px; color: #909399;">默认响应无需匹配参数</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="responseFormDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="responseSubmitLoading" @click="handleResponseSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 请求参数管理对话框 -->
    <el-dialog v-model="requestParamDialogVisible" :title="`请求参数管理 - ${currentResponse?.statusCode || ''}`" width="80%" @close="handleRequestParamDialogClose">
      <div style="margin-bottom: 16px;">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 12px;">
          <template #default>
            <p style="margin: 4px 0;">• PATH: RESTful路径参数，如 /api/users/{userId}</p>
            <p style="margin: 4px 0;">• QUERY: URL查询参数，如 ?userId=123</p>
            <p style="margin: 4px 0;">• REQUEST_BODY: 请求体参数，如 JSON body中的字段</p>
            <p style="margin: 4px 0;">• HEADER: 请求头参数</p>
            <p style="margin: 4px 0;">• FILE: 文件上传参数</p>
          </template>
        </el-alert>
        <el-button type="primary" @click="handleAddRequestParam">
          <Plus :width="'1em'" :height="'1em'" />
          添加参数
        </el-button>
      </div>

      <el-table
        v-loading="requestParamLoading"
        :data="requestParamList"
        border
        style="width: 100%;"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="paramName" label="参数名称" width="150" />
        <el-table-column prop="paramType" label="参数类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getParamTypeTagType(row.paramType)">
              {{ getParamTypeLabel(row.paramType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paramValue" label="参数值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="required" label="必填" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.required ? 'danger' : 'info'" size="small">
              {{ row.required ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDeleteRequestParam(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑请求参数对话框 -->
    <el-dialog v-model="requestParamFormDialogVisible" :title="requestParamFormTitle" width="500px" @close="handleRequestParamFormDialogClose">
      <el-form ref="requestParamFormRef" :model="requestParamForm" :rules="requestParamRules" label-width="100px">
        <el-form-item label="参数名称" prop="paramName">
          <el-input v-model="requestParamForm.paramName" placeholder="例如: userId" @blur="handleParamNameBlur" />
          <div v-show="requestParamForm.isPathParam" style="font-size: 12px; color: #909399; margin-top: 4px;">
            该参数名为 RESTful 路径参数
          </div>
        </el-form-item>
        <el-form-item label="参数类型" prop="paramType">
          <el-select v-model="requestParamForm.paramType" placeholder="请选择参数类型" style="width: 100%" :disabled="requestParamForm.isPathParam || requestParamForm.isQueryParam">
            <el-option label="PATH (RESTful路径)" value="PATH" />
            <el-option label="QUERY (URL查询参数)" value="QUERY" />
            <el-option label="REQUEST_BODY (请求体)" value="REQUEST_BODY" />
            <el-option label="HEADER (请求头)" value="HEADER" />
            <el-option label="FILE (文件上传)" value="FILE" />
          </el-select>
          <div v-show="requestParamForm.isPathParam" style="font-size: 12px; color: #909399; margin-top: 4px;">
            该参数名为 RESTful 路径参数，类型自动设置为 PATH
          </div>
          <div v-show="requestParamForm.isQueryParam" style="font-size: 12px; color: #909399; margin-top: 4px;">
            该参数名为 GET 请求查询字符串中的占位符参数，类型自动设置为 QUERY
          </div>
        </el-form-item>
        <el-form-item label="参数值" prop="paramValue">
          <el-input v-model="requestParamForm.paramValue" placeholder="例如: 123" :disabled="requestParamForm.paramType === 'FILE'" />
          <div v-show="requestParamForm.paramType === 'FILE'" style="font-size: 12px; color: #909399; margin-top: 4px;">
            文件类型不需要设置参数值
          </div>
          <div v-show="requestParamForm.paramType === 'PATH'" style="font-size: 12px; color: #909399; margin-top: 4px;">
            • 设置为"通用"或"*"：匹配任意参数值<br/>
            • 设置为具体值：仅当请求参数值等于该值时匹配
          </div>
          <div v-show="requestParamForm.paramType !== 'FILE' && requestParamForm.paramType !== 'PATH'" style="font-size: 12px; color: #909399; margin-top: 4px;">
            当请求的该参数值与此值匹配时，返回对应的响应
          </div>
        </el-form-item>
        <el-form-item label="是否必填" prop="required">
          <el-switch v-model="requestParamForm.required" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            如果勾选且请求中没有该参数，则匹配失败
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="requestParamFormDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="requestParamSubmitLoading" @click="handleRequestParamSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, InfoFilled, WarningFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useRoute } from 'vue-router'
import { getAccessibleProjects } from '@/api/project'

const route = useRoute()

// 搜索表单
const searchForm = reactive({
  name: '',
  path: '',
  projectId: null,
  method: '',
  enabled: null
})

// 项目列表
const projectList = ref([])
const accessibleProjects = ref([])

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

// 响应管理相关
const responseDialogVisible = ref(false)
const responseLoading = ref(false)
const responseFormDialogVisible = ref(false)
const responseSubmitLoading = ref(false)
const responseFormRef = ref()
const responseList = ref([])
const currentApi = ref(null)
const activeResponseId = ref(null)
const responseFormTitle = ref('添加响应')

// 表单数据
const form = reactive({
  id: null,
  projectId: null,
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
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入接口名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入接口路径', trigger: 'blur' },
    { min: 1, max: 200, message: '长度在 1 到 200 个字符', trigger: 'blur' },
    {
      pattern: /^\/[a-zA-Z0-9_/?{}?=&\-]*$/,
      message: '路径格式不正确，必须以/开头',
      trigger: 'blur'
    }
  ],
  method: [
    { required: true, message: '请选择请求方法', trigger: 'change' }
  ],
  requestType: [
    { required: true, message: '请选择请求类型', trigger: 'change' }
  ]
}

// 响应表单数据
const responseForm = reactive({
  id: null,
  statusCode: 200,
  contentType: 'application/json',
  headers: '',
  responseBody: '',
  weight: 50,
  enabled: true,
  active: false,
  isDefault: false,
  responseDelay: 0
})

// 响应表单验证规则
const responseRules = {
  statusCode: [
    { required: true, message: '请输入状态码', trigger: 'blur' }
  ],
  contentType: [
    { required: true, message: '请选择内容类型', trigger: 'change' }
  ],
  responseBody: [
    { required: true, message: '请输入响应体内容', trigger: 'blur' }
  ]
}

// 请求参数管理相关
const requestParamDialogVisible = ref(false)
const requestParamLoading = ref(false)
const requestParamFormDialogVisible = ref(false)
const requestParamSubmitLoading = ref(false)
const requestParamFormRef = ref()
const requestParamList = ref([])
const currentResponse = ref(null)
const requestParamFormTitle = ref('添加参数')

// 请求参数表单数据
const requestParamForm = reactive({
  id: null,
  paramName: '',
  paramType: 'REQUEST_BODY',
  paramValue: '',
  required: true,
  isPathParam: false,
  isQueryParam: false
})

// 请求参数表单验证规则
const requestParamRules = {
  paramName: [
    { required: true, message: '请输入参数名称', trigger: 'blur' }
  ],
  paramType: [
    { required: true, message: '请选择参数类型', trigger: 'change' }
  ],
  paramValue: [
    { required: true, message: '请输入参数值', trigger: 'blur' }
  ]
}

// 提取接口路径中的RESTful参数名称
const extractPathParamNames = (path) => {
  const paramNames = []
  const parts = path.split('/')
  for (const part of parts) {
    if (part.startsWith('{') && part.endsWith('}')) {
      paramNames.push(part.substring(1, part.length - 1))
    }
  }
  return paramNames
}

// 提取查询字符串中的占位符参数名称
const extractQueryParamNames = (path) => {
  const paramNames = []
  // 查找查询参数部分的占位符，格式如 ?name={name}&age={age}
  const queryMatch = path.match(/\?.+/)
  if (queryMatch) {
    const queryString = queryMatch[0]
    // 匹配 {paramName} 格式的占位符
    const placeholderRegex = /\{([^}]+)\}/g
    let match
    while ((match = placeholderRegex.exec(queryString)) !== null) {
      paramNames.push(match[1])
    }
  }
  return paramNames
}

// 获取参数类型标签
const getParamTypeLabel = (type) => {
  const labels = {
    'PATH': 'PATH',
    'QUERY': 'QUERY',
    'REQUEST_BODY': 'BODY',
    'HEADER': 'HEADER',
    'FILE': 'FILE'
  }
  return labels[type] || type
}

// 获取参数类型标签类型
const getParamTypeTagType = (type) => {
  const types = {
    'PATH': 'success',
    'QUERY': 'primary',
    'REQUEST_BODY': 'warning',
    'HEADER': 'info',
    'FILE': 'danger'
  }
  return types[type] || 'info'
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

// 判断当前编辑的接口是否有多个激活响应
const hasMultipleActiveResponses = ref(false)

// 检查接口是否有多个激活响应
const checkMultipleActiveResponses = async (apiId) => {
  if (!apiId) {
    hasMultipleActiveResponses.value = false
    return
  }
  try {
    const response = await request({
      url: `/mock-apis/${apiId}/responses`,
      method: 'get'
    })
    if (response.code === 200) {
      const activeCount = response.data.filter(r => r.active === true).length
      hasMultipleActiveResponses.value = activeCount > 1
    }
  } catch (error) {
    console.error('检查激活响应失败:', error)
    hasMultipleActiveResponses.value = false
  }
}

// 获取项目列表
const fetchProjects = async () => {
  try {
    const response = await getAccessibleProjects()
    if (response.code === 200) {
      projectList.value = response.data
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

const fetchAccessibleProjects = async () => {
  try {
    const response = await getAccessibleProjects()
    if (response.code === 200) {
      accessibleProjects.value = response.data
    }
  } catch (error) {
    console.error('获取可访问项目失败:', error)
  }
}

// 获取接口列表
const fetchApis = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm
    }

    // 使用搜索表单中的projectId，如果没有则使用路由参数
    let url = '/mock-apis'
    if (searchForm.projectId) {
      url = `/mock-apis/project/${searchForm.projectId}`
    } else if (route.query.projectId) {
      url = `/mock-apis/project/${route.query.projectId}`
    }

    const response = await request({
      url,
      method: 'get',
      params
    })

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
  searchForm.projectId = null
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
  form.projectId = accessibleProjects.value.length > 0 ? accessibleProjects.value[0].id : null
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
const handleEdit = async (row) => {
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

  // 检查是否有多个激活响应
  await checkMultipleActiveResponses(row.id)

  dialogVisible.value = true
}

// 管理响应
const handleResponses = async (row) => {
  currentApi.value = row
  responseDialogVisible.value = true
  await fetchResponses()
}

// 获取响应列表
const fetchResponses = async () => {
  if (!currentApi.value) return
  responseLoading.value = true
  try {
    const response = await request({
      url: `/mock-apis/${currentApi.value.id}/responses`,
      method: 'get'
    })
    if (response.code === 200) {
      responseList.value = response.data
      // 找到当前激活的响应
      const active = responseList.value.find(r => r.active === true)
      activeResponseId.value = active ? active.id : null
    } else {
      ElMessage.error('获取响应列表失败')
    }
  } catch (error) {
    console.error('获取响应列表失败:', error)
    ElMessage.error('获取响应列表失败')
  } finally {
    responseLoading.value = false
  }
}

// 根据状态码获取行样式类名
const getResponseRowClass = ({ row, rowIndex }) => {
  const statusCodes = responseList.value.map(r => r.statusCode)
  const sameStatusCount = statusCodes.filter(code => code === row.statusCode).length
  if (sameStatusCount > 1) {
    // 为相同状态码的行返回不同的背景色
    const colors = ['status-code-color-1', 'status-code-color-2', 'status-code-color-3', 'status-code-color-4', 'status-code-color-5']
    const colorIndex = statusCodes.indexOf(row.statusCode) % colors.length
    return colors[colorIndex]
  }
  return ''
}

// 添加响应
const handleAddResponse = () => {
  responseFormTitle.value = '添加响应'
  responseForm.id = null
  responseForm.statusCode = 200
  responseForm.contentType = 'application/json'
  responseForm.headers = ''
  responseForm.responseBody = ''
  responseForm.weight = 50
  responseForm.enabled = true
  responseFormDialogVisible.value = true
}

// 编辑响应
const handleEditResponse = (row) => {
  responseFormTitle.value = '编辑响应'
  responseForm.id = row.id
  responseForm.statusCode = row.statusCode
  responseForm.contentType = row.contentType
  responseForm.headers = row.headers || ''
  responseForm.responseBody = row.responseBody
  responseForm.weight = row.weight || 50
  responseForm.enabled = row.enabled
  responseForm.active = row.active || false
  responseForm.isDefault = row.isDefault || false
  responseForm.responseDelay = row.responseDelay || 0
  responseFormDialogVisible.value = true
}

// 删除响应
const handleDeleteResponse = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该响应吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await request({
      url: `/mock-apis/responses/${row.id}`,
      method: 'delete'
    })
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchResponses()
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

// 更新响应（内联编辑）
const handleUpdateResponse = async (row) => {
  try {
    const response = await request({
      url: '/mock-apis/responses',
      method: 'put',
      data: {
        id: row.id,
        statusCode: row.statusCode,
        contentType: row.contentType,
        headers: row.headers,
        responseBody: row.responseBody,
        weight: row.weight,
        enabled: row.enabled,
        mockApi: { id: currentApi.value.id }
      }
    })
    if (response.code === 200) {
      ElMessage.success('更新成功')
    } else {
      ElMessage.error('更新失败')
      fetchResponses()
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败')
    fetchResponses()
  }
}

// 设置激活响应
const handleSetActiveResponse = async (responseId) => {
  try {
    const response = await request({
      url: `/mock-apis/${currentApi.value.id}/responses/${responseId}/active`,
      method: 'post'
    })
    if (response.code === 200) {
      ElMessage.success('设置成功')
      fetchResponses()
    } else {
      ElMessage.error('设置失败')
      fetchResponses()
    }
  } catch (error) {
    console.error('设置失败:', error)
    ElMessage.error('设置失败')
    fetchResponses()
  }
}

// 切换响应激活状态
const handleToggleActive = async (row) => {
  // 判断接口是否启用了随机返回（确保转换为布尔值）
  const isRandomEnabled = currentApi.value?.enableRandom === true

  // 计算当前已激活的响应数量（不包括当前正在切换的响应）
  const currentActiveCount = responseList.value.filter(r => r.active === true && r.id !== row.id).length

  console.log('切换激活状态:', {
    isRandomEnabled,
    rowActive: row.active,
    currentActiveCount,
    isActivating: !row.active
  })

  if (!isRandomEnabled) {
    // 未启用随机返回时，不允许激活多个响应
    if (!row.active && currentActiveCount >= 1) {
      ElMessage({
        type: 'warning',
        message: '接口未启用随机返回，不允许激活多个响应。如需使用多个响应，请先启用"启用随机返回"选项。',
        duration: 5000,
        showClose: true
      })
      // 取消本次切换，恢复原状态
      await fetchResponses()
      return
    }
  }

  // 设置该响应的激活状态
  await handleSetActiveResponse(row.id)
}

// 提交响应表单
const handleResponseSubmit = async () => {
  try {
    await responseFormRef.value.validate()
    responseSubmitLoading.value = true

    const submitData = {
      ...responseForm,
      mockApi: { id: currentApi.value.id }
    }

    const response = responseForm.id
      ? await request({
          url: '/mock-apis/responses',
          method: 'put',
          data: submitData
        })
      : await request({
          url: `/mock-apis/${currentApi.value.id}/responses`,
          method: 'post',
          data: submitData
        })

    if (response.code === 200) {
      ElMessage.success(responseForm.id ? '编辑成功' : '添加成功')
      responseFormDialogVisible.value = false
      fetchResponses()
    } else {
      ElMessage.error(response.message || (responseForm.id ? '编辑失败' : '添加失败'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(responseForm.id ? '编辑失败' : '添加失败')
  } finally {
    responseSubmitLoading.value = false
  }
}

// 关闭响应对话框
const handleResponseDialogClose = () => {
  currentApi.value = null
  responseList.value = []
  activeResponseId.value = null
}

// 关闭响应表单对话框
const handleResponseFormDialogClose = () => {
  responseFormRef.value?.resetFields()
}

// 打开请求参数管理
const handleRequestParams = async (row) => {
  currentResponse.value = row
  requestParamDialogVisible.value = true
  await fetchRequestParams(row.id)
}

// 获取请求参数列表
const fetchRequestParams = async (responseId) => {
  requestParamLoading.value = true
  try {
    const response = await request({
      url: `/responses/${responseId}/params`,
      method: 'get'
    })
    if (response.code === 200) {
      requestParamList.value = response.data || []
    }
  } catch (error) {
    console.error('获取请求参数失败:', error)
    ElMessage.error('获取请求参数失败')
  } finally {
    requestParamLoading.value = false
  }
}

// 添加请求参数
const handleAddRequestParam = () => {
  requestParamFormTitle.value = '添加参数'

  const apiPath = form.path || currentApi.value?.path || ''
  const apiMethod = form.method || currentApi.value?.method || 'GET'

  // 提取路径中的RESTful参数名称
  const pathParamNames = extractPathParamNames(apiPath)

  // 提取查询字符串中的占位符参数名称（仅针对 GET 请求）
  const queryParamNames = apiMethod === 'GET' ? extractQueryParamNames(apiPath) : []

  // 合并所有占位符参数名称（优先使用查询参数）
  const allParamNames = [...queryParamNames, ...pathParamNames]

  // 如果有参数，设置第一个作为参数名称
  const defaultParamName = allParamNames.length > 0 ? allParamNames[0] : ''
  const isPathParam = pathParamNames.includes(defaultParamName)
  const isQueryParam = queryParamNames.includes(defaultParamName)

  Object.assign(requestParamForm, {
    id: null,
    paramName: defaultParamName,
    paramType: isPathParam ? 'PATH' : (isQueryParam ? 'QUERY' : 'REQUEST_BODY'),
    paramValue: '',
    required: true,
    isPathParam: isPathParam,
    isQueryParam: isQueryParam
  })
  requestParamFormDialogVisible.value = true
}

// 参数名称变化时判断是否为路径参数或查询参数
const handleParamNameBlur = () => {
  const apiPath = form.path || currentApi.value?.path || ''
  const apiMethod = form.method || currentApi.value?.method || 'GET'

  const pathParamNames = extractPathParamNames(apiPath)
  const queryParamNames = apiMethod === 'GET' ? extractQueryParamNames(apiPath) : []

  const isPathParam = pathParamNames.includes(requestParamForm.paramName)
  const isQueryParam = queryParamNames.includes(requestParamForm.paramName)

  if (isPathParam) {
    requestParamForm.paramType = 'PATH'
    requestParamForm.isPathParam = true
    requestParamForm.isQueryParam = false
  } else if (isQueryParam) {
    requestParamForm.paramType = 'QUERY'
    requestParamForm.isPathParam = false
    requestParamForm.isQueryParam = true
  } else if (requestParamForm.paramType === 'PATH' || requestParamForm.paramType === 'QUERY') {
    requestParamForm.paramType = 'REQUEST_BODY'
    requestParamForm.isPathParam = false
    requestParamForm.isQueryParam = false
  } else {
    requestParamForm.isPathParam = false
    requestParamForm.isQueryParam = false
  }
}

// 删除请求参数
const handleDeleteRequestParam = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该参数吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await request({
      url: `/responses/${currentResponse.value.id}/params/${row.id}`,
      method: 'delete'
    })
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchRequestParams(currentResponse.value.id)
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

// 提交请求参数表单
const handleRequestParamSubmit = async () => {
  if (!requestParamFormRef.value) return

  try {
    await requestParamFormRef.value.validate()

    requestParamSubmitLoading.value = true
    const response = await request({
      url: `/responses/${currentResponse.value.id}/params`,
      method: 'post',
      data: requestParamForm
    })

    if (response.code === 200) {
      ElMessage.success('添加成功')
      requestParamFormDialogVisible.value = false
      fetchRequestParams(currentResponse.value.id)
    } else {
      ElMessage.error(response.message || '添加失败')
    }
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error('添加失败')
  } finally {
    requestParamSubmitLoading.value = false
  }
}

// 关闭请求参数表单对话框
const handleRequestParamFormDialogClose = () => {
  requestParamFormRef.value?.resetFields()
}

// 关闭请求参数管理对话框
const handleRequestParamDialogClose = () => {
  requestParamList.value = []
  currentResponse.value = null
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

// 复制接口路径
const handleCopyPath = async (row) => {
  const path = `/api/mock-server/${row.project?.code}${row.path}`
  try {
    await navigator.clipboard.writeText(path)
    ElMessage.success('复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    // 创建时需要包含project对象
    const submitData = isEdit.value ? { ...form } : {
      ...form,
      project: { id: form.projectId }
    }

    const response = isEdit.value
      ? await request({
          url: '/mock-apis',
          method: 'put',
          data: submitData
        })
      : await request({
          url: '/mock-apis',
          method: 'post',
          data: submitData
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
  fetchProjects()
  fetchAccessibleProjects()
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

/* 状态码行背景色 */
:deep(.status-code-color-1) {
  background-color: #e8f5e9 !important;
}

:deep(.status-code-color-2) {
  background-color: #e3f2fd !important;
}

:deep(.status-code-color-3) {
  background-color: #fff3e0 !important;
}

:deep(.status-code-color-4) {
  background-color: #f3e5f5 !important;
}

:deep(.status-code-color-5) {
  background-color: #fce4ec !important;
}
</style>

