<template>
  <div class="forgot-password-container" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="forgot-password-overlay"></div>
    <div class="forgot-password-card">
      <router-link to="/login" class="back-link">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ $t('forgotPassword.backToLogin') }}</span>
      </router-link>
      <div class="forgot-password-header">
        <h1>{{ $t('forgotPassword.title') }}</h1>
        <p>{{ $t('forgotPassword.subtitle') }}</p>
      </div>

      <!-- 步骤1：输入邮箱 -->
      <el-form
        v-if="step === 1"
        ref="emailFormRef"
        :model="emailForm"
        :rules="emailRules"
        @submit.prevent="handleSend"
      >
        <el-form-item prop="email">
          <el-input
            v-model="emailForm.email"
            :placeholder="$t('forgotPassword.emailPlaceholder')"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="submit-button"
            native-type="submit"
          >
            {{ $t('forgotPassword.resetPassword') }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 步骤2：成功 -->
      <div v-if="step === 2" class="success-section">
        <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
        <p class="success-message">{{ $t('forgotPassword.successMessage') }}</p>
        <p class="success-hint">{{ $t('forgotPassword.successHint') }}</p>
        <el-button type="primary" size="large" class="submit-button" @click="$router.push('/login')">
          {{ $t('forgotPassword.goToLogin') }}
        </el-button>
      </div>

      <!-- 步骤3：失败提示（邮件未配置等） -->
      <div v-if="step === 3" class="error-section">
        <el-icon class="error-icon"><WarningFilled /></el-icon>
        <p class="error-message">{{ errorMessage }}</p>
        <el-button type="primary" size="large" class="submit-button" @click="step = 1">
          {{ $t('common.back') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CircleCheckFilled, WarningFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { useBingBackground } from '@/composables/useBingBackground'

const { t } = useI18n()
const { bgImage, fetchBingBg } = useBingBackground()

const step = ref(1)
const loading = ref(false)
const errorMessage = ref('')

const emailFormRef = ref()
const emailForm = reactive({
  email: ''
})

const emailRules = computed(() => ({
  email: [
    { required: true, message: t('forgotPassword.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('forgotPassword.emailInvalid'), trigger: 'blur' }
  ]
}))

const handleSend = async () => {
  if (!emailFormRef.value) return
  try {
    await emailFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const response = await axios.post('/api/auth/forgot-password', { email: emailForm.email })
    if (response.data && response.data.code === 200) {
      step.value = 2
    } else {
      errorMessage.value = response.data?.message || t('forgotPassword.sendFailed')
      step.value = 3
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || t('forgotPassword.sendFailed')
    step.value = 3
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBingBg()
})
</script>

<style scoped>
.forgot-password-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  overflow: hidden;
  transition: background-image 0.8s ease;
}

.forgot-password-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(30, 30, 60, 0.55) 0%,
    rgba(30, 30, 60, 0.35) 50%,
    rgba(30, 30, 60, 0.55) 100%
  );
  z-index: 0;
}

.forgot-password-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  text-decoration: none;
  font-size: 13px;
  margin-bottom: 12px;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: #667eea;
}

.forgot-password-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-password-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.forgot-password-header p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.submit-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.success-section,
.error-section {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 64px;
  color: #67c23a;
  margin-bottom: 16px;
}

.error-icon {
  font-size: 64px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.success-message {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.success-hint {
  font-size: 13px;
  color: #909399;
  margin-bottom: 24px;
}

.error-message {
  font-size: 15px;
  color: #f56c6c;
  margin-bottom: 24px;
}

@media (max-width: 480px) {
  .forgot-password-card {
    width: 90%;
    padding: 30px 20px;
  }

  .forgot-password-header h1 {
    font-size: 20px;
  }
}
</style>
