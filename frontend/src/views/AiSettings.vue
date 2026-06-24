<template>
  <div class="provider-settings">
    <!-- 页面头部 - 渐变横幅 -->
    <div class="page-hero">
      <div class="hero-icon">
        <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
          <defs>
            <linearGradient id="heroGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#667eea"/>
              <stop offset="100%" stop-color="#764ba2"/>
            </linearGradient>
          </defs>
          <rect x="6" y="6" width="36" height="36" rx="8" stroke="url(#heroGrad)" stroke-width="2.5" fill="none"/>
          <circle cx="24" cy="24" r="5" stroke="url(#heroGrad)" stroke-width="2" fill="none"/>
          <path d="M24 19v-5M24 34v-5M19 24h-5M34 24h-5M16.5 16.5l-3.5-3.5M31.5 16.5l3.5-3.5M16.5 31.5l-3.5 3.5M31.5 31.5l3.5 3.5" stroke="url(#heroGrad)" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <div class="hero-text">
        <h2>{{ $t('ai.title') }}</h2>
        <p>{{ $t('ai.description') }}</p>
      </div>
    </div>

    <div class="content-wrapper" v-loading="loading">
      <!-- 当前启用的服务商 - 状态卡片 -->
      <div class="status-card" :class="{ active: !!enabledProvider }">
        <div class="status-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" v-if="enabledProvider">
            <circle cx="12" cy="12" r="10" stroke="#67C23A" stroke-width="2" fill="none"/>
            <path d="M7 12l3.5 3.5L17 9" stroke="#67C23A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" v-else>
            <circle cx="12" cy="12" r="10" stroke="#909399" stroke-width="2" fill="none"/>
            <line x1="12" y1="8" x2="12" y2="13" stroke="#909399" stroke-width="2" stroke-linecap="round"/>
            <circle cx="12" cy="17" r="1" fill="#909399"/>
          </svg>
        </div>
        <div class="status-info">
          <span class="status-label">{{ $t('ai.currentProvider') }}</span>
          <span class="status-value" v-if="enabledProvider">
            <strong>{{ enabledProvider.providerName }}</strong>
            <el-tag size="small" type="success" effect="plain" round style="margin-left: 8px">
              {{ enabledProvider.defaultModel }}
            </el-tag>
          </span>
          <span class="status-value muted" v-else>{{ $t('ai.noProvider') }}</span>
        </div>
      </div>

      <!-- 服务商选择卡片 -->
      <div class="config-card">
        <div class="card-header">
          <h3>
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" class="card-icon">
              <rect x="3" y="3" width="18" height="18" rx="3" stroke="currentColor" stroke-width="1.8" fill="none"/>
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </svg>
            {{ $t('ai.providers') }}
          </h3>
        </div>
        <div class="card-body">
          <div class="provider-select-row">
            <span class="select-label">{{ $t('ai.selectProvider') }}</span>
            <el-select
              v-model="activeProvider"
              :placeholder="$t('ai.selectHint')"
              style="width: 340px"
              @change="selectProvider"
              size="large"
            >
              <el-option
                v-for="p in providerList"
                :key="p.key"
                :label="p.name"
                :value="p.key"
              >
                <span>{{ p.name }}</span>
                <el-tag
                  v-if="p.key === 'custom'"
                  size="small"
                  type="warning"
                  effect="plain"
                  style="margin-left: 8px"
                >{{ $t('ai.customTag') }}</el-tag>
                <el-tag
                  v-else-if="isPreset(p.key)"
                  size="small"
                  type="primary"
                  effect="plain"
                  style="margin-left: 8px"
                >{{ $t('ai.preset') }}</el-tag>
              </el-option>
            </el-select>
          </div>

          <!-- 选中服务商的详情 -->
          <div v-if="selectedProviderInfo" class="provider-info-bar">
            <div class="info-item">
              <span class="info-label">{{ $t('ai.providerName') }}</span>
              <span class="info-value">{{ selectedProviderInfo.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('ai.defaultModel') }}</span>
              <span class="info-value">{{ selectedProviderInfo.defaultModel || '—' }}</span>
            </div>
            <div class="info-item full">
              <span class="info-label">{{ $t('ai.apiUrl') }}</span>
              <code>{{ selectedProviderInfo.apiUrl || '—' }}</code>
            </div>
            <div v-if="selectedProviderInfo.website" class="info-item full">
              <span class="info-label">{{ $t('ai.website') }}</span>
              <el-link type="primary" :href="selectedProviderInfo.website" target="_blank" :underline="false">
                {{ selectedProviderInfo.website }}
                <el-icon style="margin-left: 4px"><Link /></el-icon>
              </el-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 配置表单卡片 -->
      <div class="config-card" v-if="activeProvider">
        <div class="card-header">
          <h3>
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" class="card-icon">
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" stroke="currentColor" stroke-width="1.8" fill="none"/>
            </svg>
            {{ $t('ai.configTitle', { name: activeProviderName }) }}
          </h3>
        </div>
        <div class="card-body">
          <el-form :model="form" label-width="130px" class="config-form" label-position="right">
            <el-form-item :label="$t('ai.providerName')" required>
              <el-input v-model="form.providerName" :placeholder="$t('ai.providerNamePlaceholder')" size="large" />
            </el-form-item>

            <el-form-item :label="$t('ai.apiUrl')" required>
              <el-input v-model="form.apiUrl" :placeholder="$t('ai.apiUrlPlaceholder')" size="large" />
              <div class="form-hint">{{ $t('ai.apiUrlHint') }}</div>
            </el-form-item>

            <el-form-item :label="$t('ai.apiKey')" required>
              <el-input
                v-model="form.apiKey"
                type="password"
                show-password
                :placeholder="$t('ai.apiKeyPlaceholder')"
                size="large"
              />
            </el-form-item>

            <el-form-item :label="$t('ai.defaultModel')">
              <el-input v-model="form.defaultModel" :placeholder="$t('ai.modelPlaceholder')" size="large" />
            </el-form-item>

            <el-form-item :label="$t('ai.timeout')" class="timeout-form-item">
              <el-input-number v-model="form.timeout" :min="30" :max="600" :step="30" size="large" />
              <span class="timeout-unit">秒</span>
              <span class="timeout-hint">{{ $t('ai.timeoutHint') }}</span>
            </el-form-item>

            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item :label="$t('ai.maxTokens')">
                  <el-input-number v-model="form.maxTokens" :min="1" :max="131072" :step="256" size="large" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('ai.temperature')">
                  <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider style="margin: 16px 0 20px" />

            <el-form-item>
              <div class="form-actions">
                <el-button type="primary" @click="saveConfig" :loading="saving" size="large">
                  <el-icon style="margin-right: 4px"><Check /></el-icon>
                  {{ $t('common.save') }}
                </el-button>
                <el-button
                  @click="testConnectivity"
                  :loading="testing"
                  size="large"
                  plain
                >
                  <el-icon style="margin-right: 4px"><Link /></el-icon>
                  {{ $t('ai.testConnectivity') }}
                </el-button>
                <el-button
                  v-if="savedConfigId && !form.enabled"
                  type="success"
                  @click="toggleEnabled"
                  :loading="toggling"
                  size="large"
                  plain
                >
                  {{ $t('ai.enable') }}
                </el-button>
                <el-button
                  v-if="savedConfigId && form.enabled"
                  type="warning"
                  @click="toggleEnabled"
                  :loading="toggling"
                  size="large"
                  plain
                >
                  {{ $t('ai.disable') }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-card" v-if="!activeProvider">
        <svg viewBox="0 0 120 120" width="100" height="100" fill="none">
          <rect x="20" y="30" width="80" height="60" rx="10" stroke="#dcdfe6" stroke-width="2" fill="none"/>
          <circle cx="45" cy="60" r="8" stroke="#dcdfe6" stroke-width="2" fill="none"/>
          <path d="M60 60h25M60 68h18" stroke="#dcdfe6" stroke-width="2" stroke-linecap="round"/>
          <circle cx="45" cy="80" r="4" fill="#e6e8eb"/>
          <rect x="55" y="77" width="30" height="6" rx="3" fill="#e6e8eb"/>
        </svg>
        <p>{{ $t('ai.selectHint') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Link, Check } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const toggling = ref(false)
const testing = ref(false)
const activeProvider = ref('')
const savedConfigId = ref(null)

// 预设服务商列表（从后端获取）
const providerList = ref([])
const savedConfigs = ref([])

const form = ref({
  provider: '',
  providerName: '',
  apiUrl: '',
  apiKey: '',
  defaultModel: '',
  maxTokens: 4096,
  temperature: 0.7,
  timeout: 120,
  enabled: false
})

const activeProviderName = computed(() => {
  const p = providerList.value.find(item => item.key === activeProvider.value)
  return p ? p.name : activeProvider.value
})

const selectedProviderInfo = computed(() => {
  return providerList.value.find(item => item.key === activeProvider.value) || null
})

const enabledProvider = computed(() => {
  return savedConfigs.value.find(c => c.enabled)
})

function isPreset(key) {
  return key !== 'custom'
}

// 加载预设服务商列表
async function loadPresetProviders() {
  try {
    const res = await request.get('/ai-config/preset-providers')
    if (res.code === 200) {
      const map = res.data
      providerList.value = Object.keys(map).map(key => ({
        key,
        name: map[key].name,
        apiUrl: map[key].apiUrl,
        defaultModel: map[key].defaultModel,
        website: map[key].website || ''
      }))
    }
  } catch (e) {
    console.error('加载预设服务商失败', e)
  }
}

// 加载已保存的配置
async function loadConfigs() {
  try {
    const res = await request.get('/ai-config')
    if (res.code === 200) {
      savedConfigs.value = res.data || []
      // 将已启用配置的超时同步到 localStorage，供 request.js 动态读取
      const enabled = savedConfigs.value.find(c => c.enabled)
      if (enabled && enabled.timeout) {
        localStorage.setItem('aiTimeout', enabled.timeout * 1000)
      }
    }
  } catch (e) {
    console.error('加载AI配置失败', e)
  }
}

// 选择服务商
function selectProvider(key) {
  activeProvider.value = key
  const preset = providerList.value.find(p => p.key === key)
  const saved = savedConfigs.value.find(c => c.provider === key)

  if (saved) {
    savedConfigId.value = saved.id
    form.value = {
      provider: saved.provider,
      providerName: saved.providerName,
      apiUrl: saved.apiUrl,
      apiKey: saved.apiKey,
      defaultModel: saved.defaultModel || '',
      maxTokens: saved.maxTokens || 4096,
      temperature: saved.temperature || 0.7,
      timeout: saved.timeout || 120,
      enabled: saved.enabled
    }
  } else {
    savedConfigId.value = null
    form.value = {
      provider: key,
      providerName: preset ? preset.name : '',
      apiUrl: preset ? preset.apiUrl : '',
      apiKey: '',
      defaultModel: preset ? preset.defaultModel : '',
      maxTokens: 4096,
      temperature: 0.7,
      timeout: 120,
      enabled: false
    }
  }
}

// 保存配置
async function saveConfig() {
  if (!form.value.apiUrl || !form.value.apiKey) {
    ElMessage.warning(t('ai.validation'))
    return
  }

  saving.value = true
  try {
    const res = await request.post('/ai-config', form.value)
    if (res.code === 200) {
      savedConfigId.value = res.data.id
      form.value.enabled = res.data.enabled
      // 实时同步 AI 超时到 localStorage，request.js 会动态读取
      const timeout = form.value.timeout || 120
      localStorage.setItem('aiTimeout', timeout * 1000)
      ElMessage.success(t('common.success'))
      await loadConfigs()
    }
  } catch (e) {
    ElMessage.error(t('common.error'))
  } finally {
    saving.value = false
  }
}

// 切换启用
async function toggleEnabled() {
  toggling.value = true
  try {
    const res = await request.put(`/ai-config/${savedConfigId.value}/toggle`)
    if (res.code === 200) {
      form.value.enabled = res.data.enabled
      ElMessage.success(form.value.enabled ? t('ai.enabled') : t('ai.disabled'))
      await loadConfigs()
    }
  } catch (e) {
    ElMessage.error(t('common.error'))
  } finally {
    toggling.value = false
  }
}

// 连通性验证
async function testConnectivity() {
  if (!form.value.apiUrl || !form.value.apiKey) {
    ElMessage.warning(t('ai.validation'))
    return
  }

  testing.value = true
  try {
    const res = await request.post('/ai-config/test-connectivity', {
      apiUrl: form.value.apiUrl,
      apiKey: form.value.apiKey,
      defaultModel: form.value.defaultModel
    })
    if (res.code === 200 && res.data) {
      if (res.data.success) {
        ElMessageBox.alert(
          t('ai.testLatency', { latency: res.data.latency, model: res.data.model }),
          t('ai.testPassed'),
          { confirmButtonText: t('common.confirm'), type: 'success' }
        )
      } else {
        ElMessageBox.alert(
          res.data.error || t('ai.testFailed'),
          t('ai.testFailed'),
          { confirmButtonText: t('common.confirm'), type: 'error' }
        )
      }
    }
  } catch (e) {
    const errorMsg = e?.response?.data?.message || e?.message || t('ai.testFailed')
    ElMessageBox.alert(errorMsg, t('ai.testFailed'), { confirmButtonText: t('common.confirm'), type: 'error' })
  } finally {
    testing.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([loadPresetProviders(), loadConfigs()])
  loading.value = false
})
</script>

<style scoped>
.provider-settings {
  padding: 0;
  max-width: 880px;
  margin: 0 auto;
}

/* ========== 页面头部横幅 ========== */
.page-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 32px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 14px;
  box-shadow: 0 4px 24px rgba(102, 126, 234, 0.25);
  position: relative;
  overflow: hidden;
}

.page-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  pointer-events: none;
}

.page-hero::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: 60%;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  pointer-events: none;
}

.hero-icon {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 14px;
  backdrop-filter: blur(4px);
  z-index: 1;
}

.hero-text {
  z-index: 1;
}

.hero-text h2 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.hero-text p {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
}

/* ========== 内容区 ========== */
.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ========== 状态卡片 ========== */
.status-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 24px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.status-card.active {
  border-color: #b7eb8f;
  box-shadow: 0 1px 8px rgba(103, 194, 58, 0.08);
}

.status-icon {
  flex-shrink: 0;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.status-value {
  font-size: 14px;
  color: #303133;
}

.status-value.muted {
  color: #c0c4cc;
}

/* ========== 配置卡片 ========== */
.config-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.card-header {
  padding: 18px 24px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-icon {
  color: #667eea;
  flex-shrink: 0;
}

.card-body {
  padding: 24px;
}

/* ========== 服务商选择行 ========== */
.provider-select-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.select-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  font-weight: 500;
}

/* ========== 服务商信息条 ========== */
.provider-info-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
}

.info-item {
  width: 50%;
  padding: 8px 0;
}

.info-item.full {
  width: 100%;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-right: 8px;
}

.info-value {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.provider-info-bar code {
  font-size: 12px;
  background: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  color: #606266;
  border: 1px solid #e4e7ed;
  word-break: break-all;
}

/* ========== 配置表单 ========== */
.config-form {
  max-width: 100%;
}

.config-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* ========== 超时字段 ========== */
.timeout-unit {
  margin-left: 10px;
  color: #606266;
  font-size: 13px;
}

.timeout-hint {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.timeout-form-item :deep(.el-input-number) {
  width: 150px;
}

/* ========== 空状态卡片 ========== */
.empty-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 60px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-card p {
  margin: 0;
  font-size: 14px;
  color: #c0c4cc;
}

/* ========== 分割线覆盖 ========== */
.config-card :deep(.el-divider--horizontal) {
  margin: 16px 0 20px;
}
</style>
