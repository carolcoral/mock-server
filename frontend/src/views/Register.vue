<template>
  <div class="login-container" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="login-overlay"></div>
    <div class="login-card">
      <router-link to="/" class="back-home-link">
        <el-icon><ArrowLeft /></el-icon>
        <span>{{ $t('common.backHome') }}</span>
      </router-link>
      <div class="login-header">
        <h1>
          <svg class="login-logo-icon" viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
          Mock Server
        </h1>
        <p>{{ $t('register.subtitle') }}</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            :placeholder="$t('register.usernamePlaceholder')"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            :placeholder="$t('register.emailPlaceholder')"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            :placeholder="$t('register.passwordPlaceholder')"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            :placeholder="$t('register.confirmPasswordPlaceholder')"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            native-type="submit"
          >
            {{ $t('register.registerButton') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link-wrapper">
        <router-link to="/login" class="register-link">{{ $t('login.toLogin') }}</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { useBingBackground } from '@/composables/useBingBackground'

const { t } = useI18n()
const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)

const { bgImage, fetchBingBg } = useBingBackground()

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error(t('register.confirmPasswordRequired')))
  } else if (value !== registerForm.password) {
    callback(new Error(t('register.passwordMismatch')))
  } else {
    callback()
  }
}

const rules = computed(() => ({
  username: [
    { required: true, message: t('register.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 50, message: t('register.usernameLength'), trigger: 'blur' }
  ],
  email: [
    { required: true, message: t('register.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('register.emailInvalid'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('register.passwordRequired'), trigger: 'blur' },
    { min: 8, message: t('register.passwordMinLength'), trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}))

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const response = await axios.post('/api/auth/register', {
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })

    if (response.data && response.data.code === 200) {
      ElMessage.success(t('register.registerSuccess'))
      router.push('/login')
    } else {
      ElMessage.error(response.data?.message || t('register.registerFailed'))
    }
  } catch (error) {
    const msg = error.response?.data?.message || error.message || t('register.registerFailed')
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBingBg()
})
</script>

<style scoped>
.login-container {
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

.login-overlay {
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

.login-card {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.back-home-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  text-decoration: none;
  font-size: 13px;
  margin-bottom: 12px;
  transition: color 0.3s ease;
}

.back-home-link:hover {
  color: #667eea;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  margin: 0 0 10px 0;
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(
    135deg,
    #667eea 0%,
    #e83e8c 25%,
    #fd7e14 50%,
    #764ba2 75%,
    #20c997 100%
  );
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradientShift 4s ease infinite;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.login-logo-icon {
  flex-shrink: 0;
  color: #667eea;
  -webkit-text-fill-color: #667eea;
  animation: iconPulse 3s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes iconPulse {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.08); }
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.login-form {
  margin-bottom: 30px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.register-link-wrapper {
  text-align: center;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 30px 20px;
  }

  .login-header h1 {
    font-size: 24px;
  }
}
</style>
