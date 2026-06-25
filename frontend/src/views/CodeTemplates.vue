<template>
  <div class="code-templates">
    <div class="page-header">
      <h1>{{ $t('codeTemplate.title') }}</h1>
      <div style="display: flex; gap: 8px;">
        <el-button v-if="isAdmin && selectedRows.length > 0" type="danger" @click="handleBatchDelete">
          {{ $t('codeTemplate.batchDelete') }} ({{ selectedRows.length }})
        </el-button>
        <el-button type="primary" @click="handleCreate">
          <Plus :width="'1em'" :height="'1em'" />
          {{ $t('codeTemplate.createTemplate') }}
        </el-button>
      </div>
    </div>

    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.name" :placeholder="$t('codeTemplate.searchByName')" clearable @clear="handleSearch" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.projectId" :placeholder="$t('codeTemplate.searchByProject')" clearable @change="handleSearch" style="width: 100%" filterable>
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.enabled" :placeholder="$t('codeTemplate.searchByStatus')" clearable @change="handleSearch" style="width: 100%">
            <el-option :label="$t('codeTemplate.enabled')" :value="true" />
            <el-option :label="$t('codeTemplate.disabled')" :value="false" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">{{ $t('codeTemplate.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('codeTemplate.reset') }}</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="templateList"
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="isAdmin" type="selection" width="50" />
        <el-table-column prop="id" :label="$t('codeTemplate.id')" width="80" />
        <el-table-column prop="name" :label="$t('codeTemplate.name')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span>
              <el-tag v-if="row.isSystem" type="danger" size="small" effect="dark" style="margin-right: 6px;">{{ $t('codeTemplate.systemDefault') }}</el-tag>
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('codeTemplate.project')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.isSystem" type="danger" size="small">{{ $t('codeTemplate.systemDefault') }}</el-tag>
            <el-tag v-else type="primary" size="small">{{ row.project?.name || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('codeTemplate.description')" min-width="220" show-overflow-tooltip />
        <el-table-column prop="enabled" :label="$t('codeTemplate.status')" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? $t('codeTemplate.enabled') : $t('codeTemplate.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('codeTemplate.createTime')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('codeTemplate.actions')" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd) => handleActionCommand(cmd, row)">
              <el-button type="primary" link>
                {{ $t('codeTemplate.more') }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">{{ $t('codeTemplate.viewCode') }}</el-dropdown-item>
                  <el-dropdown-item command="edit" :disabled="!canEditTemplate(row)">{{ $t('codeTemplate.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="delete" :disabled="!canEditTemplate(row)" divided style="color: #f56c6c;">{{ $t('codeTemplate.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="!isEdit && isAdmin" :label="$t('codeTemplate.systemDefault')">
          <el-switch v-model="form.isSystem" :active-text="$t('codeTemplate.isSystemTemplate')" :inactive-text="$t('codeTemplate.isProjectTemplate')" />
        </el-form-item>
        <el-form-item :label="$t('codeTemplate.project')" prop="projectId" v-if="!isEdit && !form.isSystem">
          <el-select v-model="form.projectId" :placeholder="$t('codeTemplate.selectProject')" clearable style="width: 100%" filterable>
            <el-option
              v-for="project in accessibleProjects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('codeTemplate.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('codeTemplate.namePlaceholder')" :disabled="form.isSystem && !isAdmin" />
        </el-form-item>
        <el-form-item :label="$t('codeTemplate.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="$t('codeTemplate.descriptionPlaceholder')" :disabled="form.isSystem && !isAdmin" />
        </el-form-item>
        <el-form-item :label="$t('codeTemplate.status')" prop="enabled">
          <el-switch v-model="form.enabled" :active-text="$t('codeTemplate.enabled')" :inactive-text="$t('codeTemplate.disabled')" :disabled="form.isSystem && !isAdmin" />
        </el-form-item>
        <el-divider content-position="left">
          <span style="font-size: 14px; font-weight: 600; color: #303133;">{{ $t('codeTemplate.javaSourceCode') }}</span>
          <el-tag v-if="form.isSystem && !isAdmin" type="danger" size="small" style="margin-left: 8px;">{{ $t('codeTemplate.systemReadonly') }}</el-tag>
        </el-divider>
        <!-- AI 生成代码模板区域 -->
        <div v-if="!form.isSystem || isAdmin" style="margin-bottom: 12px; padding: 12px; background: #fafbfc; border: 1px solid #e4e7ed; border-radius: 6px;">
          <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
            <span style="font-size: 13px; font-weight: 600; color: #606266; white-space: nowrap;">{{ $t('ai.transformerType') }}：</span>
            <el-select v-model="aiTransformerType" :placeholder="$t('ai.selectTransformerType')" size="small" style="width: 220px;">
              <el-option :label="$t('ai.transformerResponseWrapping')" value="response_wrapping" />
              <el-option :label="$t('ai.transformerDataMasking')" value="data_masking" />
              <el-option :label="$t('ai.transformerFieldTransform')" value="field_transform" />
              <el-option :label="$t('ai.transformerConditionalResponse')" value="conditional_response" />
              <el-option :label="$t('ai.transformerLogging')" value="logging" />
              <el-option :label="$t('ai.transformerHttpForward')" value="http_forward" />
            </el-select>
            <el-button type="warning" size="small" @click="handleAiGenerateCode" :loading="aiCodeLoading">
              <MagicStick :width="'0.9em'" :height="'0.9em'" style="margin-right: 4px;" />
              {{ aiCodeLoading ? $t('ai.generatingCode') : $t('ai.generateCodeTemplate') }}
            </el-button>
            <span style="font-size: 12px; color: #909399;">基于接口信息智能生成 CustomResponseTransformer 实现代码</span>
          </div>
        </div>
        <div style="margin-bottom: 8px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
          <el-button v-if="!form.isSystem || isAdmin" size="small" @click="loadDefaultTemplateCode">{{ $t('codeTemplate.useDefaultTemplate') }}</el-button>
          <el-button size="small" type="success" @click="validateTemplateCode" :loading="validatingCode">{{ $t('codeTemplate.compileValidate') }}</el-button>
          <span v-if="validationResult" :style="{ color: validationResult.success ? '#67C23A' : '#F56C6C', fontSize: '13px', marginLeft: '8px' }">
            {{ validationResult.success ? $t('codeTemplate.validatePassed') : $t('codeTemplate.validateFailed') + validationResult.message }}
          </span>
          <el-button size="small" style="margin-left: auto;" @click="codeFullscreen = !codeFullscreen">
            {{ codeFullscreen ? $t('common.exitFullscreen') : $t('common.fullscreen') }}
          </el-button>
        </div>
        <el-form-item prop="sourceCode" label-width="0" :class="{ 'code-fullscreen': codeFullscreen }">
          <div v-if="codeFullscreen" class="fullscreen-toolbar">
            <el-button v-if="!form.isSystem || isAdmin" size="small" @click="loadDefaultTemplateCode">{{ $t('codeTemplate.useDefaultTemplate') }}</el-button>
            <el-button size="small" type="success" @click="validateTemplateCode" :loading="validatingCode">{{ $t('codeTemplate.compileValidate') }}</el-button>
            <el-button size="small" style="margin-left: auto;" @click="codeFullscreen = false">{{ $t('common.exitFullscreen') }}</el-button>
          </div>
          <MonacoEditor
            ref="monacoEditorRef"
            v-model="form.sourceCode"
            :read-only="form.isSystem && !isAdmin"
            :height="codeFullscreen ? 'calc(100vh - 60px)' : '420px'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('codeTemplate.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('codeTemplate.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 查看代码对话框 -->
    <el-dialog v-model="viewDialogVisible" :title="$t('codeTemplate.viewDialogTitle')" width="800px">
      <div style="margin-bottom: 12px;">
        <div>
          <span style="font-weight: 600;">{{ $t('codeTemplate.templateNameLabel') }}</span>
          <span>{{ viewingTemplate?.name }}</span>
          <span style="margin-left: 20px; font-weight: 600;">{{ $t('codeTemplate.projectLabel') }}</span>
          <el-tag v-if="viewingTemplate?.isSystem" type="danger" size="small">{{ $t('codeTemplate.systemDefault') }}</el-tag>
          <el-tag v-else type="primary" size="small">{{ viewingTemplate?.project?.name }}</el-tag>
        </div>
        <div style="margin-top: 8px;">
          <span style="font-weight: 600;">{{ $t('codeTemplate.descriptionLabel') }}</span>
          <span>{{ viewingTemplate?.description || '—' }}</span>
        </div>
      </div>
      <MonacoEditor
        :model-value="viewingTemplate?.sourceCode"
        :read-only="true"
        height="500px"
      />
      <template #footer>
        <el-button @click="viewDialogVisible = false">{{ $t('codeTemplate.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown, MagicStick } from '@element-plus/icons-vue'
import { formatTime, loadDateFormat } from '@/utils/dateFormat'
import { useI18n } from 'vue-i18n'
import { getAccessibleProjects, getAllAccessibleProjects } from '@/api/project'
import {
  getAccessibleTemplates,
  getTemplatesByProjectId,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  batchDeleteTemplates,
  validateTemplateSourceCode
} from '@/api/codeTemplate'
import { generateCodeTemplate } from '@/api/ai'
import { defineAsyncComponent } from 'vue'

const MonacoEditor = defineAsyncComponent(() => import('@/components/MonacoEditor.vue'))

const { t } = useI18n()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

// 搜索表单
const searchForm = reactive({
  name: '',
  projectId: null,
  enabled: null
})

// 项目列表（用于筛选和创建选择）
const projectList = ref([])
const accessibleProjects = ref([])
// 项目角色映射 projectId -> userRole
const projectRoleMap = ref({})

// 表格数据
const loading = ref(false)
const templateList = ref([])

// 多选
const selectedRows = ref([])
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()

// AI 代码生成相关
const aiCodeLoading = ref(false)
const aiTransformerType = ref('response_wrapping')

// 编译验证相关
const validatingCode = ref(false)
const validationResult = ref(null)

// 代码编辑器全屏
const codeFullscreen = ref(false)
const monacoEditorRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  description: '',
  sourceCode: '',
  enabled: true,
  projectId: null,
  isSystem: false
})

const rules = computed(() => ({
  projectId: form.isSystem ? [] : [
    { required: true, message: t('codeTemplate.projectRequired'), trigger: 'change' }
  ],
  name: [
    { required: true, message: t('codeTemplate.nameRequired'), trigger: 'blur' },
    { min: 1, max: 200, message: t('codeTemplate.nameLength'), trigger: 'blur' }
  ],
  sourceCode: [
    { required: true, message: t('codeTemplate.sourceCodeRequired'), trigger: 'blur' }
  ]
}))

// 查看对话框
const viewDialogVisible = ref(false)
const viewingTemplate = ref(null)

// 获取项目列表
const fetchProjects = async () => {
  try {
    const response = await getAllAccessibleProjects()
    if (response.code === 200) {
      const projects = response.data || []
      projectList.value = projects
      accessibleProjects.value = projects
      // 构建项目角色映射
      const roleMap = {}
      projects.forEach(p => {
        roleMap[p.id] = p.userRole
      })
      projectRoleMap.value = roleMap
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 获取模板列表（支持服务端分页和过滤）
const fetchTemplates = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.pageSize
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.projectId) params.projectId = searchForm.projectId
    if (searchForm.enabled !== null && searchForm.enabled !== '') params.enabled = searchForm.enabled

    const response = await getAccessibleTemplates(params)
    if (response.code === 200) {
      templateList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    } else {
      ElMessage.error(t('codeTemplate.fetchFailed'))
    }
  } catch (error) {
    console.error('获取模板列表失败:', error)
    ElMessage.error(t('codeTemplate.fetchFailed'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchTemplates()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.projectId = null
  searchForm.enabled = null
  pagination.current = 1
  fetchTemplates()
}

// 分页
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.current = 1
  fetchTemplates()
}

const handleCurrentChange = (page) => {
  pagination.current = page
  fetchTemplates()
}

// 创建模板
const handleCreate = () => {
  dialogTitle.value = t('codeTemplate.createTitle')
  isEdit.value = false
  form.id = null
  form.name = ''
  form.description = ''
  form.sourceCode = ''
  form.enabled = true
  form.isSystem = false
  form.projectId = accessibleProjects.value.length > 0 ? accessibleProjects.value[0].id : null
  validationResult.value = null
  dialogVisible.value = true
}

// 编辑模板
const handleEdit = (row) => {
  dialogTitle.value = t('codeTemplate.editTitle')
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  form.sourceCode = row.sourceCode || ''
  form.enabled = row.enabled
  form.isSystem = row.isSystem || false
  form.projectId = row.project?.id
  validationResult.value = null
  dialogVisible.value = true
}

// 加载默认模板代码（与自定义接口中的默认代码模板一致）
const loadDefaultTemplateCode = () => {
  validationResult.value = null
  form.sourceCode = `import com.carolcoral.mockserver.dto.MockRequest;
import com.carolcoral.mockserver.dto.MockResponseDTO;
import com.carolcoral.mockserver.plugin.CustomResponseTransformer;
import java.util.*;
import com.alibaba.fastjson.JSON;

/**
 * 自定义响应处理器模板
 * <p>
 * 实现 {@link CustomResponseTransformer} 接口，对接口返回的报文进行自定义处理。
 * 该类会在每次匹配到对应接口时被调用，可以在 transform 方法中对响应数据进行
 * 包装、转换、脱敏、追加字段等任意操作。
 * </p>
 *
 * <h3>使用说明</h3>
 * <ol>
 *   <li>修改类名和 getDescription() 返回值以匹配你的业务场景</li>
 *   <li>在 transform() 方法中编写自定义处理逻辑</li>
 *   <li>点击"编译验证"按钮检查代码是否正确</li>
 *   <li>保存接口配置后，每次请求该接口都会自动调用此处理器</li>
 * </ol>
 *
 * <h3>可用数据源</h3>
 * <ul>
 *   <li>{@code mockResponse.getStatusCode()} — HTTP 状态码</li>
 *   <li>{@code mockResponse.getHeaders()} — 响应头 Map</li>
 *   <li>{@code mockResponse.getBody()} — 响应体（JSON 字符串或 Map/List 对象）</li>
 *   <li>{@code mockResponse.getDelay()} — 响应延迟（毫秒）</li>
 *   <li>{@code mockRequest.getPath()} — 请求路径</li>
 *   <li>{@code mockRequest.getMethod()} — 请求方法（GET/POST/PUT/DELETE）</li>
 *   <li>{@code mockRequest.getHeaders()} — 请求头 Map</li>
 *   <li>{@code mockRequest.getParams()} — URL 查询参数 Map</li>
 *   <li>{@code mockRequest.getBody()} — 请求体</li>
 *   <li>{@code mockRequest.getProjectCode()} — 项目编码</li>
 *   <li>{@code mockRequest.getPathParams()} — RESTful 路径参数 Map</li>
 *   <li>{@code apiName} — 接口名称，可用于日志记录</li>
 *   <li>{@code apiPath} — 接口路径，可用于日志记录</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>禁止使用反射、文件IO、网络、线程、脚本执行等危险API</li>
 *   <li>返回值不能为 null，必须返回有效的 MockResponseDTO 对象</li>
 *   <li>建议使用 MockResponseDTO.builder() 链式构建返回对象</li>
 *   <li>代码长度不超过 50000 字符</li>
 * </ul>
 *
 * @author 请填写你的名字
 * @version 1.0
 */
public class MyCustomTransformer implements CustomResponseTransformer {

    /**
     * 对 Mock 响应进行自定义转换处理
     * <p>
     * 该方法在基础响应流程（响应匹配、延迟计算、响应体解析等）完成之后调用，
     * 可以在此方法中对响应体、状态码、响应头等进行任意修改。
     * </p>
     *
     * @param mockResponse 经过基础流程处理后的 Mock 响应对象
     *                     <ul>
     *                       <li>{@code getStatusCode()} — HTTP 状态码（如 200, 404, 500）</li>
     *                       <li>{@code getHeaders()} — 响应头键值对</li>
     *                       <li>{@code getBody()} — 响应体，可能是 String、Map 或 List</li>
     *                       <li>{@code getDelay()} — 预设的响应延迟（毫秒），null 表示无延迟</li>
     *                     </ul>
     * @param mockRequest  原始 Mock 请求对象
     *                     <ul>
     *                       <li>{@code getPath()} — 请求路径，如 "/api/user/login"</li>
     *                       <li>{@code getMethod()} — 请求方法，如 "GET", "POST"</li>
     *                       <li>{@code getHeaders()} — 请求头键值对</li>
     *                       <li>{@code getParams()} — URL 查询参数键值对</li>
     *                       <li>{@code getBody()} — 请求体内容</li>
     *                       <li>{@code getProjectCode()} — 项目编码</li>
     *                       <li>{@code getPathParams()} — RESTful 路径参数，如 /user/{id} 中的 id</li>
     *                     </ul>
     * @param apiName      接口名称，如 "用户登录接口"，可用于日志记录
     * @param apiPath      接口路径，如 "/api/user/login"，可用于日志记录
     * @return 转换后的 MockResponseDTO 对象，不能返回 null
     *         <p>建议使用 MockResponseDTO.builder() 构建：</p>
     *         <pre>{@code
     *         return MockResponseDTO.builder()
     *                 .statusCode(200)
     *                 .headers(mockResponse.getHeaders())
     *                 .body(result)
     *                 .delay(mockResponse.getDelay())
     *                 .build();
     *         }</pre>
     */
    @Override
    public MockResponseDTO transform(MockResponseDTO mockResponse, MockRequest mockRequest,
                                      String apiName, String apiPath) {
        // 获取原始响应体
        Object body = mockResponse.getBody();

        // 在这里编写自定义处理逻辑
        // 例如：包装响应、修改字段、添加时间戳、数据脱敏、条件判断等

        // 示例：将响应包装为统一的标准格式 { code, message, data, timestamp }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", mockResponse.getStatusCode());
        result.put("message", "success");
        result.put("data", body);
        result.put("timestamp", System.currentTimeMillis());

        // 使用 Builder 模式构建新的响应对象
        // 保留原有的状态码、响应头和延迟设置，只替换响应体
        return MockResponseDTO.builder()
                .statusCode(mockResponse.getStatusCode())
                .headers(mockResponse.getHeaders())
                .body(result)
                .delay(mockResponse.getDelay())
                .build();
    }

    /**
     * 获取转换器描述信息
     * <p>
     * 该描述会在管理界面中展示，帮助区分不同的自定义处理器。
     * 建议使用简短的中文描述，如"标准格式包装器"、"数据脱敏处理器"等。
     * </p>
     *
     * @return 转换器描述字符串，不能为 null
     */
    @Override
    public String getDescription() {
        return "标准格式包装器 - 将响应包装为 {code, message, data, timestamp} 格式";
    }
}`
}

// AI 智能生成代码模板
const handleAiGenerateCode = async () => {
  if (!form.name || !form.name.trim()) {
    ElMessage.warning(t('codeTemplate.nameRequired'))
    return
  }

  aiCodeLoading.value = true
  validationResult.value = null
  try {
    const response = await generateCodeTemplate({
      apiMethod: 'POST',
      apiPath: '/api/' + form.name.trim().toLowerCase().replace(/\s+/g, '-'),
      apiName: form.name.trim(),
      description: form.description || undefined,
      transformerType: aiTransformerType.value,
      existingSourceCode: form.sourceCode || undefined
    })

    console.log('[AI CodeTemplate] response:', response)
    console.log('[AI CodeTemplate] response.code:', response.code, 'type:', typeof response.code)
    console.log('[AI CodeTemplate] response.data type:', typeof response.data, 'length:', response.data ? response.data.length : 0)

    if (response.code === 200 && response.data) {
      // 先设置 reactive form.sourceCode（用于表单验证和提交）
      form.sourceCode = response.data
      console.log('[AI CodeTemplate] form.sourceCode set, length:', form.sourceCode.length)
      // 直接通过 Monaco Editor 实例设置内容，确保编辑器显示更新
      if (monacoEditorRef.value) {
        const editorInstance = monacoEditorRef.value.getEditor()
        if (editorInstance) {
          editorInstance.setValue(response.data)
          console.log('[AI CodeTemplate] editor.setValue() called successfully')
        } else {
          console.warn('[AI CodeTemplate] monacoEditorRef.getEditor() returned null')
        }
      } else {
        console.warn('[AI CodeTemplate] monacoEditorRef is null')
      }
      ElMessage.success(t('ai.codeTemplateGenerated'))
      // 自动触发编译验证
      setTimeout(() => validateTemplateCode(), 300)
    } else {
      console.error('[AI CodeTemplate] failed: code=', response.code, 'data=', response.data, 'message=', response.message)
      ElMessage.error(response.message || t('ai.generateFailed'))
    }
  } catch (error) {
    console.error('AI 生成代码模板失败:', error)
    console.error('AI 生成代码模板失败 - 错误详情:', {
      message: error?.message,
      response: error?.response?.data,
      status: error?.response?.status,
      stack: error?.stack
    })
    ElMessage.error(t('ai.generateFailed'))
  } finally {
    aiCodeLoading.value = false
  }
}

// 编译验证模板源码
const validateTemplateCode = async () => {
  if (!form.sourceCode || !form.sourceCode.trim()) {
    validationResult.value = { success: false, message: t('codeTemplate.enterCodeFirst') }
    return
  }
  validatingCode.value = true
  validationResult.value = null
  try {
    const response = await validateTemplateSourceCode({
      sourceCode: form.sourceCode,
      templateId: form.id || undefined
    })
    if (response.code === 200) {
      validationResult.value = { success: true, message: response.data || t('codeTemplate.validateSuccessMsg') }
      ElMessage.success({ message: t('codeTemplate.validateSuccessMsg'), duration: 5000 })
    } else {
      validationResult.value = { success: false, message: response.message || t('codeTemplate.validateFailedMsg') }
      ElMessage.error({ message: response.message || t('codeTemplate.validateFailedMsg'), duration: 5000 })
    }
  } catch (error) {
    validationResult.value = { success: false, message: error.message || t('codeTemplate.validateFailedMsg') }
    ElMessage.error({ message: t('codeTemplate.validateFailed') + (error.message || ''), duration: 5000 })
  } finally {
    validatingCode.value = false
  }
}

// 查看模板代码
const handleView = (row) => {
  viewingTemplate.value = row
  viewDialogVisible.value = true
}

// 删除模板
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(t('codeTemplate.deleteConfirm', { name: row.name }), t('codeTemplate.deleteWarning'), {
      confirmButtonText: t('codeTemplate.confirmDelete'),
      cancelButtonText: t('codeTemplate.cancelDelete'),
      type: 'error',
      confirmButtonClass: 'el-button--danger'
    })

    const response = await deleteTemplate(row.id)
    if (response.code === 200) {
      ElMessage.success(t('codeTemplate.deleteSuccess'))
      fetchTemplates()
    } else {
      ElMessage.error(response.message || t('codeTemplate.deleteFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(t('codeTemplate.deleteFailed'))
    }
  }
}

// 批量删除模板（仅管理员）
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      t('codeTemplate.batchDeleteConfirm', { count: selectedRows.value.length }),
      t('codeTemplate.deleteWarning'),
      {
        confirmButtonText: t('codeTemplate.confirmDelete'),
        cancelButtonText: t('codeTemplate.cancelDelete'),
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const ids = selectedRows.value.map(row => row.id)
    const response = await batchDeleteTemplates(ids)
    if (response.code === 200) {
      ElMessage.success(t('codeTemplate.batchDeleteSuccess'))
      selectedRows.value = []
      fetchTemplates()
    } else {
      ElMessage.error(response.message || t('codeTemplate.batchDeleteFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(t('codeTemplate.batchDeleteFailed'))
    }
  }
}

// 操作分发
const handleActionCommand = (command, row) => {
  switch (command) {
    case 'view':
      handleView(row)
      break
    case 'edit':
      handleEdit(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // 如果有源代码，必须先编译验证通过
    if (form.sourceCode && form.sourceCode.trim()) {
      const validateResp = await validateTemplateSourceCode({
        sourceCode: form.sourceCode,
        templateId: form.id || undefined
      })
      if (validateResp.code !== 200) {
        validationResult.value = { success: false, message: validateResp.message || t('codeTemplate.validateFailedMsg') }
        ElMessage.error(t('codeTemplate.compileNotPassed') + (validateResp.message || ''))
        return
      }
    }

    submitLoading.value = true

    const submitData = isEdit.value
      ? {
          id: form.id,
          name: form.name,
          description: form.description,
          sourceCode: form.sourceCode,
          enabled: form.enabled,
          isSystem: form.isSystem
        }
      : (form.isSystem
        ? {
            name: form.name,
            description: form.description,
            sourceCode: form.sourceCode,
            enabled: form.enabled,
            isSystem: true
          }
        : {
            name: form.name,
            description: form.description,
            sourceCode: form.sourceCode,
            enabled: form.enabled,
            project: { id: form.projectId }
          })

    const response = isEdit.value
      ? await updateTemplate(submitData)
      : await createTemplate(submitData)

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? t('codeTemplate.editSuccess') : t('codeTemplate.createSuccess'))
      dialogVisible.value = false
      fetchTemplates()
    } else {
      ElMessage.error(response.message || (isEdit.value ? t('codeTemplate.editFailed') : t('codeTemplate.createFailed')))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? t('codeTemplate.editFailed') : t('codeTemplate.createFailed'))
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 判断用户是否可以编辑/删除模板
const canEditTemplate = (template) => {
  if (isAdmin.value) return true
  const projectId = template.project?.id
  if (!projectId) return false
  const role = projectRoleMap.value[projectId]
  return role === 'ADMIN' || role === 'CREATOR'
}

onMounted(async () => {
  await loadDateFormat()
  await fetchProjects()
  fetchTemplates()
})
</script>

<style scoped>
.code-templates {
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

/* 代码编辑器全屏 */
.code-fullscreen {
  position: fixed !important;
  inset: 0 !important;
  z-index: 9999 !important;
  background: #f0f2f5 !important;
  margin: 0 !important;
  padding: 0 !important;
  display: flex !important;
  flex-direction: column !important;
}

.code-fullscreen :deep(.el-form-item__content) {
  margin-left: 0 !important;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.fullscreen-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.fullscreen-toolbar + .monaco-editor-wrapper {
  flex: 1;
}
</style>
