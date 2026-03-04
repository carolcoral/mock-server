<template>
  <div class="settings">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>系统设置</h1>
    </div>

    <el-row :gutter="20">
      <!-- 左侧菜单 -->
      <el-col :span="6">
        <el-card class="menu-card">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="basic">
              <Setting :width="'1em'" :height="'1em'" />
              <span>基础设置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <Lock :width="'1em'" :height="'1em'" />
              <span>安全配置</span>
            </el-menu-item>
            <el-menu-item index="jwt">
              <Key :width="'1em'" :height="'1em'" />
              <span>JWT配置</span>
            </el-menu-item>
            <el-menu-item index="mock">
              <Connection :width="'1em'" :height="'1em'" />
              <span>Mock配置</span>
            </el-menu-item>
            <el-menu-item index="system">
              <InfoFilled :width="'1em'" :height="'1em'" />
              <span>系统信息</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :span="18">
        <el-card class="content-card">
          <!-- 基础设置 -->
          <div v-if="activeMenu === 'basic'">
            <h2>基础设置</h2>
            <el-divider />
            <el-form :model="basicSettings" label-width="150px">
              <el-form-item label="应用名称">
                <el-input v-model="basicSettings.appName" disabled />
              </el-form-item>
              <el-form-item label="应用版本">
                <el-input v-model="basicSettings.version" disabled />
              </el-form-item>
              <el-form-item label="系统语言">
                <el-select v-model="basicSettings.language" style="width: 100%">
                  <el-option label="中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期格式">
                <el-select v-model="basicSettings.dateFormat" style="width: 100%">
                  <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
                  <el-option label="DD/MM/YYYY" value="DD/MM/YYYY" />
                  <el-option label="MM/DD/YYYY" value="MM/DD/YYYY" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveBasicSettings" :loading="saving">保存设置</el-button>
                <el-button @click="resetBasicSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全配置 -->
          <div v-if="activeMenu === 'security'">
            <h2>安全配置</h2>
            <el-divider />
            <el-alert title="以下配置项需要重启应用后生效" type="warning" :closable="false" show-icon />
            <br />
            <el-form :model="securitySettings" label-width="180px">
              <el-form-item label="密码强度要求">
                <el-checkbox v-model="securitySettings.requireUppercase" disabled>必须包含大写字母</el-checkbox>
                <el-checkbox v-model="securitySettings.requireLowercase" disabled>必须包含小写字母</el-checkbox>
                <el-checkbox v-model="securitySettings.requireDigit" disabled>必须包含数字</el-checkbox>
                <el-checkbox v-model="securitySettings.requireSpecial" disabled>必须包含特殊字符</el-checkbox>
              </el-form-item>
              <el-form-item label="密码最小长度">
                <el-input-number v-model="securitySettings.minPasswordLength" :min="8" :max="32" disabled />
              </el-form-item>
              <el-form-item label="登录失败锁定次数">
                <el-input-number v-model="securitySettings.maxLoginAttempts" :min="3" :max="10" />
              </el-form-item>
              <el-form-item label="锁定时间（分钟）">
                <el-input-number v-model="securitySettings.lockoutDuration" :min="5" :max="60" />
              </el-form-item>
              <el-form-item label="启用IP白名单">
                <el-switch v-model="securitySettings.enableIpWhitelist" />
              </el-form-item>
              <el-form-item label="IP白名单" v-if="securitySettings.enableIpWhitelist">
                <el-input
                  v-model="securitySettings.ipWhitelist"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入IP地址，多个IP用逗号分隔"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveSecuritySettings" :loading="saving">保存设置</el-button>
                <el-button @click="resetSecuritySettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- JWT配置 -->
          <div v-if="activeMenu === 'jwt'">
            <h2>JWT配置</h2>
            <el-divider />
            <el-alert title="修改JWT配置后，所有用户需要重新登录" type="warning" :closable="false" show-icon />
            <br />
            <el-form :model="jwtSettings" label-width="180px">
              <el-form-item label="Token过期时间">
                <el-input-number v-model="jwtSettings.tokenExpiration" :min="900" :max="86400" step="300" />
                <span style="margin-left: 10px; color: #909399;">秒（建议15-30分钟）</span>
              </el-form-item>
              <el-form-item label="Refresh Token过期时间">
                <el-input-number v-model="jwtSettings.refreshTokenExpiration" :min="3600" :max="604800" step="3600" />
                <span style="margin-left: 10px; color: #909399;">秒（建议7天）</span>
              </el-form-item>
              <el-form-item label="签发者">
                <el-input v-model="jwtSettings.issuer" placeholder="请输入JWT签发者" />
              </el-form-item>
              <el-form-item label="受众">
                <el-input v-model="jwtSettings.audience" placeholder="请输入JWT受众" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveJwtSettings" :loading="saving">保存设置</el-button>
                <el-button @click="resetJwtSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- Mock配置 -->
          <div v-if="activeMenu === 'mock'">
            <h2>Mock配置</h2>
            <el-divider />
            <el-form :model="mockSettings" label-width="180px">
              <el-form-item label="默认响应延迟">
                <el-input-number v-model="mockSettings.defaultResponseDelay" :min="0" :max="5000" step="100" />
                <span style="margin-left: 10px; color: #909399;">毫秒</span>
              </el-form-item>
              <el-form-item label="最大响应延迟">
                <el-input-number v-model="mockSettings.maxResponseDelay" :min="1000" :max="10000" step="500" />
                <span style="margin-left: 10px; color: #909399;">毫秒（防止DoS攻击）</span>
              </el-form-item>
              <el-form-item label="启用请求日志">
                <el-switch v-model="mockSettings.enableRequestLog" />
              </el-form-item>
              <el-form-item label="日志保留天数">
                <el-input-number v-model="mockSettings.logRetentionDays" :min="1" :max="90" />
                <span style="margin-left: 10px; color: #909399;">天</span>
              </el-form-item>
              <el-form-item label="启用随机返回">
                <el-switch v-model="mockSettings.enableRandomResponse" />
              </el-form-item>
              <el-form-item label="最大请求体大小">
                <el-input-number v-model="mockSettings.maxRequestBodySize" :min="1" :max="100" />
                <span style="margin-left: 10px; color: #909399;">MB</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveMockSettings" :loading="saving">保存设置</el-button>
                <el-button @click="resetMockSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 系统信息 -->
          <div v-if="activeMenu === 'system'">
            <h2>系统信息</h2>
            <el-divider />
            <el-descriptions :column="2" border>
              <el-descriptions-item label="系统版本">{{ systemInfo.version }}</el-descriptions-item>
              <el-descriptions-item label="构建时间">{{ systemInfo.buildTime }}</el-descriptions-item>
              <el-descriptions-item label="运行环境">{{ systemInfo.environment }}</el-descriptions-item>
              <el-descriptions-item label="运行时间">{{ systemInfo.uptime }}</el-descriptions-item>
              <el-descriptions-item label="Java版本">{{ systemInfo.javaVersion }}</el-descriptions-item>
              <el-descriptions-item label="Spring Boot版本">{{ systemInfo.springBootVersion }}</el-descriptions-item>
              <el-descriptions-item label="数据库类型">{{ systemInfo.databaseType }}</el-descriptions-item>
              <el-descriptions-item label="数据库版本">{{ systemInfo.databaseVersion }}</el-descriptions-item>
              <el-descriptions-item label="操作系统">{{ systemInfo.osName }}</el-descriptions-item>
              <el-descriptions-item label="系统架构">{{ systemInfo.osArch }}</el-descriptions-item>
            </el-descriptions>

            <h3 style="margin-top: 30px;">性能监控</h3>
            <el-divider />
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="CPU使用率" :value="systemInfo.cpuUsage" suffix="%" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="内存使用率" :value="systemInfo.memoryUsage" suffix="%" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="磁盘使用率" :value="systemInfo.diskUsage" suffix="%" />
              </el-col>
            </el-row>

            <h3 style="margin-top: 30px;">环境变量（部分）</h3>
            <el-divider />
            <el-table :data="envVars" border style="width: 100%">
              <el-table-column prop="key" label="变量名" width="200" />
              <el-table-column prop="value" label="值" />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Lock, Key, Connection, InfoFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 当前激活的菜单
const activeMenu = ref('basic')

// 保存加载状态
const saving = ref(false)

// 基础设置
const basicSettings = reactive({
  appName: 'Mock Server',
  version: 'v1.0.0',
  language: 'zh-CN',
  dateFormat: 'YYYY-MM-DD'
})

// 安全配置
const securitySettings = reactive({
  requireUppercase: true,
  requireLowercase: true,
  requireDigit: true,
  requireSpecial: true,
  minPasswordLength: 8,
  maxLoginAttempts: 5,
  lockoutDuration: 15,
  enableIpWhitelist: false,
  ipWhitelist: ''
})

// JWT配置
const jwtSettings = reactive({
  tokenExpiration: 1800, // 30分钟
  refreshTokenExpiration: 604800, // 7天
  issuer: 'mock-server',
  audience: 'mock-server-users'
})

// Mock配置
const mockSettings = reactive({
  defaultResponseDelay: 0,
  maxResponseDelay: 5000,
  enableRequestLog: true,
  logRetentionDays: 30,
  enableRandomResponse: false,
  maxRequestBodySize: 10
})

// 系统信息
const systemInfo = reactive({
  version: '1.0.0',
  buildTime: '2026-03-03 10:00:00',
  environment: 'development',
  uptime: '2天 3小时 15分钟',
  javaVersion: '17.0.8',
  springBootVersion: '3.2.0',
  databaseType: 'SQLite',
  databaseVersion: '3.40.1',
  osName: 'Linux',
  osArch: 'amd64',
  cpuUsage: 45,
  memoryUsage: 62,
  diskUsage: 38
})

// 环境变量
const envVars = ref([
  { key: 'SPRING_PROFILES_ACTIVE', value: 'development' },
  { key: 'JAVA_OPTS', value: '-Xmx512m -Xms256m' },
  { key: 'TZ', value: 'Asia/Shanghai' },
  { key: 'LOG_LEVEL', value: 'INFO' }
])

// 菜单切换
const handleMenuSelect = (index) => {
  activeMenu.value = index
}

// 保存基础设置
const saveBasicSettings = async () => {
  saving.value = true
  try {
    // TODO: 调用API保存设置
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('基础设置已保存')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置基础设置
const resetBasicSettings = () => {
  basicSettings.language = 'zh-CN'
  basicSettings.dateFormat = 'YYYY-MM-DD'
  ElMessage.info('已重置为默认值')
}

// 保存安全配置
const saveSecuritySettings = async () => {
  saving.value = true
  try {
    // TODO: 调用API保存设置
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('安全配置已保存')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置安全配置
const resetSecuritySettings = () => {
  securitySettings.maxLoginAttempts = 5
  securitySettings.lockoutDuration = 15
  securitySettings.enableIpWhitelist = false
  securitySettings.ipWhitelist = ''
  ElMessage.info('已重置为默认值')
}

// 保存JWT配置
const saveJwtSettings = async () => {
  saving.value = true
  try {
    // TODO: 调用API保存设置
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('JWT配置已保存')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置JWT配置
const resetJwtSettings = () => {
  jwtSettings.tokenExpiration = 1800
  jwtSettings.refreshTokenExpiration = 604800
  jwtSettings.issuer = 'mock-server'
  jwtSettings.audience = 'mock-server-users'
  ElMessage.info('已重置为默认值')
}

// 保存Mock配置
const saveMockSettings = async () => {
  saving.value = true
  try {
    // TODO: 调用API保存设置
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('Mock配置已保存')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置Mock配置
const resetMockSettings = () => {
  mockSettings.defaultResponseDelay = 0
  mockSettings.maxResponseDelay = 5000
  mockSettings.enableRequestLog = true
  mockSettings.logRetentionDays = 30
  mockSettings.enableRandomResponse = false
  mockSettings.maxRequestBodySize = 10
  ElMessage.info('已重置为默认值')
}

// 获取系统信息
const fetchSystemInfo = async () => {
  try {
    // TODO: 调用API获取真实的系统信息
    // const response = await axios.get('/api/settings/system-info')
    // if (response.code === 200) {
    //   Object.assign(systemInfo, response.data)
    // }
  } catch (error) {
    console.error('获取系统信息失败:', error)
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchSystemInfo()
})
</script>

<style scoped>
.settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.menu-card {
  margin-bottom: 20px;
}

.content-card {
  margin-bottom: 20px;
  min-height: 600px;
}

h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-descriptions-item__label) {
  font-weight: 600;
  width: 150px;
  text-align: right;
}
</style>

