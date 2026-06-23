<template>
  <div class="ai-settings">
    <div class="page-header">
      <h2>{{ $t('ai.title') }}</h2>
      <p class="page-desc">{{ $t('ai.description') }}</p>
    </div>

    <div class="content-card" v-loading="loading">
      <!-- 当前启用的服务商 -->
      <div class="section">
        <div class="section-header">
          <h3>{{ $t('ai.currentProvider') }}</h3>
          <el-tag v-if="enabledProvider" type="success" size="large" effect="dark">
            {{ enabledProvider.providerName }} ({{ enabledProvider.defaultModel }})
          </el-tag>
          <el-tag v-else type="info" size="large">{{ $t('ai.noProvider') }}</el-tag>
        </div>
      </div>

      <el-divider />

      <!-- 服务商选择 -->
      <div class="section">
        <h3>{{ $t('ai.providers') }}</h3>
        <el-form label-width="100px" class="select-form">
          <el-form-item :label="$t('ai.selectProvider')">
            <el-select
              v-model="activeProvider"
              :placeholder="$t('ai.selectHint')"
              style="width: 360px"
              @change="selectProvider"
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
                  style="margin-left: 8px"
                >{{ $t('ai.customTag') }}</el-tag>
                <el-tag
                  v-else-if="isPreset(p.key)"
                  size="small"
                  type="primary"
                  style="margin-left: 8px"
                >{{ $t('ai.preset') }}</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>

        <!-- 选中服务商的详情 -->
        <div v-if="selectedProviderInfo" class="provider-detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item :label="$t('ai.providerName')">
              {{ selectedProviderInfo.name }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('ai.defaultModel')">
              {{ selectedProviderInfo.defaultModel || '—' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('ai.apiUrl')" :span="2">
              <code>{{ selectedProviderInfo.apiUrl || '—' }}</code>
            </el-descriptions-item>
            <el-descriptions-item v-if="selectedProviderInfo.website" :label="$t('ai.website')" :span="2">
              <el-link type="primary" :href="selectedProviderInfo.website" target="_blank">
                {{ selectedProviderInfo.website }}
                <el-icon style="margin-left: 2px"><Link /></el-icon>
              </el-link>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <el-divider />

      <!-- 配置表单 -->
      <div class="section" v-if="activeProvider">
        <h3>{{ $t('ai.configTitle', { name: activeProviderName }) }}</h3>

        <el-form :model="form" label-width="120px" class="config-form">
          <el-form-item :label="$t('ai.providerName')" required>
            <el-input v-model="form.providerName" :placeholder="$t('ai.providerNamePlaceholder')" />
          </el-form-item>

          <el-form-item :label="$t('ai.apiUrl')" required>
            <el-input v-model="form.apiUrl" :placeholder="$t('ai.apiUrlPlaceholder')" />
            <div class="form-hint">{{ $t('ai.apiUrlHint') }}</div>
          </el-form-item>

          <el-form-item :label="$t('ai.apiKey')" required>
            <el-input
              v-model="form.apiKey"
              type="password"
              show-password
              :placeholder="$t('ai.apiKeyPlaceholder')"
            />
          </el-form-item>

          <el-form-item :label="$t('ai.defaultModel')">
            <el-input v-model="form.defaultModel" :placeholder="$t('ai.modelPlaceholder')" />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item :label="$t('ai.maxTokens')">
                <el-input-number v-model="form.maxTokens" :min="1" :max="131072" :step="256" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="$t('ai.temperature')">
                <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button type="primary" @click="saveConfig" :loading="saving">
              {{ $t('common.save') }}
            </el-button>
            <el-button
              @click="testConnectivity"
              :loading="testing"
              :type="testResult ? (testResult.success ? 'success' : 'danger') : 'default'"
            >
              {{ testResult ? (testResult.success ? $t('ai.testPassed') : $t('ai.testFailed')) : $t('ai.testConnectivity') }}
            </el-button>
            <el-button
              v-if="savedConfigId && !form.enabled"
              type="success"
              @click="toggleEnabled"
              :loading="toggling"
            >
              {{ $t('ai.enable') }}
            </el-button>
            <el-button
              v-if="savedConfigId && form.enabled"
              type="warning"
              @click="toggleEnabled"
              :loading="toggling"
            >
              {{ $t('ai.disable') }}
            </el-button>
          </el-form-item>
          <!-- 验证结果详情 -->
          <el-form-item v-if="testResult" label=" ">
            <el-alert
              :title="testResult.success ? $t('ai.testPassed') : $t('ai.testFailed')"
              :description="testResult.success
                ? $t('ai.testLatency', { latency: testResult.latency, model: testResult.model })
                : testResult.error"
              :type="testResult.success ? 'success' : 'error'"
              :closable="false"
              show-icon
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 空状态提示 -->
      <el-empty v-if="!activeProvider" :description="$t('ai.selectHint')" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Link } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const toggling = ref(false)
const testing = ref(false)
const testResult = ref(null)
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
    }
  } catch (e) {
    console.error('加载AI配置失败', e)
  }
}

// 选择服务商
function selectProvider(key) {
  activeProvider.value = key
  testResult.value = null
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
  testResult.value = null
  try {
    const res = await request.post('/ai-config/test-connectivity', {
      apiUrl: form.value.apiUrl,
      apiKey: form.value.apiKey,
      defaultModel: form.value.defaultModel
    })
    if (res.code === 200 && res.data) {
      testResult.value = res.data
      if (res.data.success) {
        ElMessage.success(t('ai.testPassed'))
      } else {
        ElMessage.error(res.data.error || t('ai.testFailed'))
      }
    }
  } catch (e) {
    testResult.value = { success: false, error: e?.response?.data?.message || e?.message || t('ai.testFailed') }
    ElMessage.error(testResult.value.error)
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
.ai-settings {
  padding: 20px;
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 6px;
  color: #303133;
}

.page-desc {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section {
  margin-bottom: 8px;
}

.section h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #303133;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.section-header h3 {
  margin-bottom: 0;
}

.select-form {
  margin-bottom: 16px;
}

.provider-detail {
  margin-top: 16px;
}

.provider-detail code {
  font-size: 12px;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  word-break: break-all;
}

.config-form {
  max-width: 600px;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.el-divider {
  margin: 20px 0;
}
</style>
