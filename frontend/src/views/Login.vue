<template>
  <div class="login-container" :style="{ backgroundImage: `url(${bgImage})` }">
    <div class="login-overlay"></div>
    <div class="login-card">
      <div class="login-header">
        <h1>
          <svg class="login-logo-icon" viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
          Mock Server
        </h1>
        <p>{{ $t('login.subtitle') }}</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            :placeholder="$t('login.usernamePlaceholder')"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            :placeholder="$t('login.passwordPlaceholder')"
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
            {{ $t('login.loginButton') }}
          </el-button>
        </el-form-item>
      </el-form>
      
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

// 默认背景图片（本地静态资源）
const DEFAULT_BG = '/default-bg.jpg'
const bgImage = ref(DEFAULT_BG)

// Bing 每日图片代理接口（后端 Spring Boot Controller 转发，绕过 CORS）
const BING_PROXY_PATH = '/bing-hp'
const FETCH_TIMEOUT = 2000 // 2秒超时

/**
 * 从 Bing 获取每日推荐风景图片
 * 通过后端代理转发请求到 Bing API，避免浏览器跨域限制
 * 超过 2 秒未获取成功则使用默认背景图
 */
const fetchBingBg = async () => {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), FETCH_TIMEOUT)

  try {
    const response = await fetch(BING_PROXY_PATH, {
      signal: controller.signal
    })
    clearTimeout(timeoutId)

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const data = await response.json()
    const images = data?.images || []

    if (images.length === 0) {
      throw new Error('No images returned')
    }

    // 随机选择一张
    const randomIndex = Math.floor(Math.random() * images.length)
    const image = images[randomIndex]

    // Bing API 返回的 url 是相对路径如 "/th?id=OHR.xxx_1920x1080.jpg"
    const baseUrl = image.urlbase || image.url?.replace(/&pid=hp/, '')
    const fullUrl = baseUrl?.startsWith('http')
      ? baseUrl
      : `https://cn.bing.com${baseUrl}_1920x1080.jpg`

    // 预加载验证图片可用（<img> 加载不受 CORS 限制，仅用于 CSS background-image）
    const img = new Image()
    img.src = fullUrl

    await new Promise((resolve, reject) => {
      img.onload = () => {
        bgImage.value = fullUrl
        resolve()
      }
      img.onerror = () => reject(new Error('Image load failed'))
      setTimeout(() => reject(new Error('Image load timeout')), 2000)
    })
  } catch (error) {
    console.warn('Bing 每日图片获取失败，使用默认背景:', error.message || error)
    bgImage.value = DEFAULT_BG
  }
}

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则（使用 computed 确保响应语言切换）
const rules = computed(() => ({
  username: [
    { required: true, message: t('login.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 50, message: t('login.usernameLength'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.passwordRequired'), trigger: 'blur' },
    { min: 8, message: t('login.passwordMinLength'), trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/,
      message: t('login.passwordStrengthHint'),
      trigger: 'blur'
    }
  ]
}))

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
  } catch {
    // 表单验证失败，不需要额外提示（element-plus会自动显示验证错误）
    return
  }
  
  loading.value = true
  try {
    const result = await userStore.login(loginForm.username, loginForm.password)
    
    if (result.success) {
      ElMessage.success(t('login.loginSuccess'))
      router.push('/dashboard')
    } else {
      ElMessage.error(result.message || t('login.loginFailed'))
    }
  } catch (error) {
    ElMessage.error(error.message || t('login.networkError'))
  } finally {
    loading.value = false
  }
}

// 页面加载时获取 Bing 每日图片作为背景
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

/* 半透明遮罩层，确保登录卡片清晰可读 */
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
