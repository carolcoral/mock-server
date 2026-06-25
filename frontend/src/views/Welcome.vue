<template>
  <div class="welcome-page">
    <!-- 背景装饰 -->
    <div class="bg-decor">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
      <div class="bg-grid"></div>
    </div>

    <!-- 导航栏 -->
    <header class="welcome-header">
      <div class="header-inner">
        <div class="header-brand">
          <svg class="brand-icon" viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
          <span class="brand-name">Mock Server</span>
        </div>
        <div class="header-actions">
          <a class="header-nav-link" @click="goChangelog">
            <svg class="nav-link-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            <span>{{ $t('welcome.changelog') }}</span>
          </a>
          <el-select v-model="currentLocale" size="small" @change="switchLocale" class="locale-select" popper-class="dark-locale-popper">
            <el-option label="中文" value="zh-CN" />
            <el-option label="English" value="en-US" />
            <el-option label="日本語" value="ja-JP" />
          </el-select>
        </div>
      </div>
    </header>

    <!-- 主体 -->
    <main class="welcome-main">
      <!-- Hero 区域 -->
      <section class="hero-section">
        <div class="hero-badge">{{ welcomeBadge }}</div>
        <h1 class="hero-title">{{ $t('welcome.title') }}</h1>
        <p class="hero-subtitle">{{ $t('welcome.subtitle') }}</p>
        <p class="hero-desc">{{ $t('welcome.description') }}</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round class="hero-cta" @click="goLogin">
            {{ $t('welcome.cta') }}
            <el-icon class="cta-icon"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </section>

      <!-- 特性卡片 8个 -->
      <section class="features-section">
        <div class="section-header">
          <h2 class="section-title">{{ $t('welcome.featuresTitle') }}</h2>
          <p class="section-subtitle">Mock Server 为 API 开发全流程提供一站式解决方案</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="(feature, idx) in features" :key="idx" :style="{ '--delay': idx * 0.05 + 's' }">
            <div class="feature-icon" :style="{ background: feature.bg }">
              <el-icon :size="22">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </div>
      </section>

      <!-- 快速上手流程 -->
      <section class="workflow-section">
        <div class="section-header">
          <h2 class="section-title">{{ $t('welcome.workflowTitle') }}</h2>
        </div>
        <div class="workflow-steps">
          <div class="workflow-step" v-for="(step, idx) in workflowSteps" :key="idx">
            <div class="step-number">{{ idx + 1 }}</div>
            <div class="step-line" v-if="idx < workflowSteps.length - 1">
              <svg viewBox="0 0 120 24" width="120" height="24">
                <line x1="0" y1="12" x2="100" y2="12" stroke="rgba(102,126,234,0.3)" stroke-width="1.5" stroke-dasharray="4 4"/>
                <polyline points="100,6 112,12 100,18" fill="none" stroke="rgba(102,126,234,0.5)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="step-content">
              <h4 class="step-title">{{ step.title }}</h4>
              <p class="step-desc">{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- AI 能力矩阵 -->
      <section class="ai-section">
        <div class="section-header">
          <h2 class="section-title">
            <svg class="ai-sparkle" viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2l1.5 5.5L19 9l-5.5 1.5L12 16l-1.5-5.5L5 9l5.5-1.5z"/>
              <path d="M18 16l.75 2.25L21 19l-2.25.75L18 22l-.75-2.25L15 19l2.25-.75z"/>
              <path d="M6 18l.5 1.5L8 20l-1.5.5L6 22l-.5-1.5L4 20l1.5-.5z"/>
            </svg>
            {{ $t('welcome.aiTitle') }}
          </h2>
          <p class="section-subtitle">{{ $t('welcome.aiDesc') }}</p>
        </div>
        <div class="ai-caps-grid">
          <div class="ai-cap-card" v-for="(cap, idx) in aiCaps" :key="idx">
            <div class="ai-cap-icon">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
            <span class="ai-cap-text">{{ cap }}</span>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部 -->
    <footer class="welcome-footer">
      <div class="footer-inner">
        <p class="footer-copy">{{ $t('welcome.footer') }}</p>
        <div class="footer-links">
          <a class="footer-link" @click="goChangelog">{{ $t('welcome.changelog') }}</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import {
  ArrowRight,
  Connection,
  EditPen,
  TrendCharts,
  Upload,
  Document,
  UserFilled,
  ChatDotSquare,
  MagicStick
} from '@element-plus/icons-vue'

const { t, locale } = useI18n()
const router = useRouter()

const currentLocale = ref(locale.value)
const appVersion = ref('')

const switchLocale = (val) => {
  locale.value = val
  localStorage.setItem('locale', val)
}

const goLogin = () => {
  router.push('/login')
}

const goChangelog = () => {
  router.push('/changelog')
}

const fetchVersion = async () => {
  try {
    const response = await request.get('/system/version')
    if (response.code === 200 && response.data) {
      appVersion.value = response.data.version || ''
    }
  } catch (error) {
    console.error('获取版本号失败:', error)
  }
}

const welcomeBadge = computed(() => {
  const ver = appVersion.value ? 'v' + appVersion.value : ''
  const desc = t('welcome.badge')
  return ver ? ver + ' · ' + desc : desc
})

onMounted(() => {
  fetchVersion()
})

const features = computed(() => [
  {
    icon: Connection,
    title: t('welcome.feature1Title'),
    desc: t('welcome.feature1Desc'),
    bg: 'linear-gradient(135deg, #667eea, #764ba2)'
  },
  {
    icon: EditPen,
    title: t('welcome.feature2Title'),
    desc: t('welcome.feature2Desc'),
    bg: 'linear-gradient(135deg, #f093fb, #f5576c)'
  },
  {
    icon: MagicStick,
    title: t('welcome.feature3Title'),
    desc: t('welcome.feature3Desc'),
    bg: 'linear-gradient(135deg, #f6d365, #fda085)'
  },
  {
    icon: Upload,
    title: t('welcome.feature4Title'),
    desc: t('welcome.feature4Desc'),
    bg: 'linear-gradient(135deg, #4facfe, #00f2fe)'
  },
  {
    icon: TrendCharts,
    title: t('welcome.feature5Title'),
    desc: t('welcome.feature5Desc'),
    bg: 'linear-gradient(135deg, #43e97b, #38f9d7)'
  },
  {
    icon: Document,
    title: t('welcome.feature6Title'),
    desc: t('welcome.feature6Desc'),
    bg: 'linear-gradient(135deg, #a18cd1, #fbc2eb)'
  },
  {
    icon: UserFilled,
    title: t('welcome.feature7Title'),
    desc: t('welcome.feature7Desc'),
    bg: 'linear-gradient(135deg, #ff9a9e, #fecfef)'
  },
  {
    icon: ChatDotSquare,
    title: t('welcome.feature8Title'),
    desc: t('welcome.feature8Desc'),
    bg: 'linear-gradient(135deg, #89f7fe, #66a6ff)'
  }
])

const workflowSteps = computed(() => [
  { title: t('welcome.workflow1Title'), desc: t('welcome.workflow1Desc') },
  { title: t('welcome.workflow2Title'), desc: t('welcome.workflow2Desc') },
  { title: t('welcome.workflow3Title'), desc: t('welcome.workflow3Desc') },
  { title: t('welcome.workflow4Title'), desc: t('welcome.workflow4Desc') }
])

const aiCaps = computed(() => [
  t('welcome.aiCap1'),
  t('welcome.aiCap2'),
  t('welcome.aiCap3'),
  t('welcome.aiCap4')
])
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(170deg, #0a0a1a 0%, #12122e 25%, #1a1a3e 50%, #16213e 75%, #0f3460 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  position: relative;
}

/* 背景装饰 */
.bg-decor {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.12;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: #667eea;
  top: -150px;
  right: -100px;
  animation: orbFloat1 12s ease-in-out infinite;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: #764ba2;
  bottom: 20%;
  left: -120px;
  animation: orbFloat2 15s ease-in-out infinite;
}

.orb-3 {
  width: 350px;
  height: 350px;
  background: #4facfe;
  top: 50%;
  right: -80px;
  animation: orbFloat3 10s ease-in-out infinite;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
  background-size: 60px 60px;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-30px, 40px) scale(1.05); }
  66% { transform: translate(20px, -20px) scale(0.95); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(40px, -30px) scale(1.06); }
  66% { transform: translate(-20px, 20px) scale(0.94); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-25px, -35px) scale(1.04); }
}

/* 导航栏 */
.welcome-header {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  background: rgba(10, 10, 26, 0.72);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 32px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  color: #667eea;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #e83e8c, #20c997);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: brandShift 5s ease infinite;
}

@keyframes brandShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-nav-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.3s;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
}

.header-nav-link:hover {
  color: rgba(255, 255, 255, 0.9);
  border-bottom-color: rgba(102, 126, 234, 0.5);
}

.nav-link-icon {
  flex-shrink: 0;
  opacity: 0.8;
}

.header-nav-link:hover .nav-link-icon {
  opacity: 1;
}

/* 语言选择器 */
.locale-select {
  width: 110px;
}

.locale-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: none;
  transition: background 0.25s, border-color 0.25s;
}

.locale-select :deep(.el-input__wrapper:hover),
.locale-select :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(102, 126, 234, 0.4);
}

.locale-select :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  text-align: center;
  cursor: pointer;
}

.locale-select :deep(.el-input__suffix) {
  color: rgba(255, 255, 255, 0.55);
}

.locale-select :deep(.el-input .el-input__icon) {
  color: rgba(255, 255, 255, 0.55);
}

/* 主体 */
.welcome-main {
  flex: 1;
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  padding-bottom: 40px;
}

/* Hero 区域 */
.hero-section {
  text-align: center;
  padding: clamp(48px, 8vh, 96px) 24px clamp(32px, 5vh, 56px);
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 18px;
  margin-bottom: 28px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #a78bfa;
  background: rgba(167, 139, 250, 0.1);
  border: 1px solid rgba(167, 139, 250, 0.2);
  letter-spacing: 0.3px;
}

.hero-badge::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #a78bfa;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.hero-title {
  font-size: clamp(36px, 6vw, 60px);
  font-weight: 800;
  line-height: 1.15;
  margin: 0 0 16px;
  background: linear-gradient(135deg, #fff 0%, #c4b5fd 35%, #818cf8 65%, #fff 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: heroShift 6s ease infinite;
}

@keyframes heroShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.hero-subtitle {
  font-size: clamp(18px, 2.5vw, 24px);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 14px;
}

.hero-desc {
  font-size: clamp(14px, 1.8vw, 16px);
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.75;
  margin: 0 auto 40px;
  max-width: 600px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
}

.hero-cta {
  padding: 14px 40px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  box-shadow: 0 4px 24px rgba(102, 126, 234, 0.35);
  transition: transform 0.3s ease, box-shadow 0.3s ease !important;
}

.hero-cta:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.5) !important;
}

.cta-icon {
  margin-left: 4px;
}

/* 通用 section header */
.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: clamp(24px, 3.5vw, 32px);
  font-weight: 700;
  margin: 0 0 10px;
  color: rgba(255, 255, 255, 0.9);
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.section-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
  line-height: 1.6;
}

/* 特性区域 */
.features-section {
  padding: 40px 24px 48px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 28px 22px;
  text-align: center;
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1),
              border-color 0.35s ease,
              background 0.35s ease,
              box-shadow 0.35s ease;
  animation: fadeInUp 0.6s ease backwards;
  animation-delay: var(--delay, 0s);
}

.feature-card:hover {
  transform: translateY(-6px);
  border-color: rgba(102, 126, 234, 0.25);
  background: rgba(102, 126, 234, 0.05);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #fff;
  transition: transform 0.3s ease;
}

.feature-card:hover .feature-icon {
  transform: scale(1.08);
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 8px;
  color: rgba(255, 255, 255, 0.88);
}

.feature-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  line-height: 1.6;
  margin: 0;
}

/* 快速上手 */
.workflow-section {
  padding: 48px 24px;
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.workflow-steps {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 0;
  flex-wrap: wrap;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  flex: 1;
  min-width: 200px;
  max-width: 280px;
  padding: 0 12px;
  position: relative;
}

.step-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16px;
  position: relative;
  z-index: 2;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.step-line {
  position: absolute;
  top: 24px;
  left: calc(50% + 36px);
  width: calc(100% - 72px);
  display: flex;
  align-items: center;
  z-index: 1;
}

.step-content {
  padding: 0 8px;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88);
  margin: 0 0 6px;
}

.step-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  line-height: 1.6;
  margin: 0;
}

/* AI 能力矩阵 */
.ai-section {
  padding: 48px 24px 56px;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.ai-sparkle {
  color: #f6d365;
  flex-shrink: 0;
}

.ai-caps-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.ai-cap-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 20px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  cursor: default;
}

.ai-cap-card:hover {
  background: rgba(102, 126, 234, 0.06);
  border-color: rgba(102, 126, 234, 0.2);
}

.ai-cap-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(246, 211, 101, 0.2), rgba(253, 160, 133, 0.15));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f6d365;
  flex-shrink: 0;
}

.ai-cap-text {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.4;
}

/* 底部 */
.welcome-footer {
  position: relative;
  z-index: 1;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 20px 24px;
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-copy {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
  margin: 0;
}

.footer-links {
  display: flex;
  gap: 16px;
}

.footer-link {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.35);
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-link:hover {
  color: rgba(255, 255, 255, 0.6);
}

/* ========== 响应式 ========== */
@media (max-width: 1100px) {
  .features-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .ai-caps-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header-inner {
    padding: 0 20px;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }

  .workflow-steps {
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }

  .workflow-step {
    max-width: 100%;
    flex-direction: row;
    text-align: left;
    gap: 16px;
    padding: 0;
  }

  .step-number {
    margin-bottom: 0;
    flex-shrink: 0;
  }

  .step-line {
    display: none;
  }

  .step-content {
    padding: 0;
  }

  .ai-caps-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .hero-cta,
  .hero-login {
    width: 100%;
    max-width: 280px;
    justify-content: center;
  }

  .footer-inner {
    flex-direction: column;
    text-align: center;
  }
}

@media (max-width: 480px) {
  .header-inner {
    padding: 0 14px;
  }

  .brand-name {
    font-size: 17px;
  }

  .features-grid {
    grid-template-columns: 1fr;
    max-width: 360px;
    margin: 0 auto;
  }

  .ai-caps-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    margin-bottom: 32px;
  }

  .hero-section {
    padding: 40px 20px 36px;
  }
}
</style>
