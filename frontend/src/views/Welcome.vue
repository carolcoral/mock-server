<template>
  <div class="welcome-page">
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
        <div class="header-locale">
          <el-select v-model="currentLocale" size="small" @change="switchLocale" class="locale-select">
            <el-option label="中文" value="zh-CN" />
            <el-option label="English" value="en-US" />
            <el-option label="日本語" value="ja-JP" />
          </el-select>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="welcome-body">
      <!-- Hero 区域 -->
      <section class="hero-section">
        <div class="hero-badge">{{ $t('welcome.badge') }}</div>
        <h1 class="hero-title">{{ $t('welcome.title') }}</h1>
        <p class="hero-subtitle">{{ $t('welcome.subtitle') }}</p>
        <p class="hero-desc">{{ $t('welcome.description') }}</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round class="hero-cta" @click="goLogin">
            {{ $t('welcome.cta') }}
            <el-icon class="cta-icon"><Right /></el-icon>
          </el-button>
        </div>
      </section>

      <!-- 特性卡片 -->
      <section class="features-section">
        <h2 class="section-title">{{ $t('welcome.featuresTitle') }}</h2>
        <div class="features-grid">
          <div class="feature-card" v-for="(feature, idx) in features" :key="idx">
            <div class="feature-icon" :style="{ background: feature.bg }">
              <el-icon :size="24">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </div>
      </section>

      <!-- 底部 -->
      <footer class="welcome-footer">
        <p>{{ $t('welcome.footer') }}</p>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  Right,
  Connection,
  EditPen,
  TrendCharts,
  Monitor
} from '@element-plus/icons-vue'

const { t, locale } = useI18n()
const router = useRouter()

const currentLocale = ref(locale.value)

const switchLocale = (val) => {
  locale.value = val
  localStorage.setItem('locale', val)
}

const goLogin = () => {
  router.push('/login')
}

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
    icon: TrendCharts,
    title: t('welcome.feature3Title'),
    desc: t('welcome.feature3Desc'),
    bg: 'linear-gradient(135deg, #4facfe, #00f2fe)'
  },
  {
    icon: Monitor,
    title: t('welcome.feature4Title'),
    desc: t('welcome.feature4Desc'),
    bg: 'linear-gradient(135deg, #43e97b, #38f9d7)'
  }
])
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #0c0c1e 0%, #1a1a3e 30%, #16213e 60%, #0f3460 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  position: relative;
}

/* 背景装饰光晕 */
.welcome-page::before {
  content: '';
  position: absolute;
  top: -200px;
  right: -150px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
  pointer-events: none;
}

.welcome-page::after {
  content: '';
  position: absolute;
  bottom: -100px;
  left: -100px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(118, 75, 162, 0.12) 0%, transparent 70%);
  pointer-events: none;
}

/* 导航栏 */
.welcome-header {
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(12px);
  background: rgba(12, 12, 30, 0.75);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
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

.locale-select {
  width: 110px;
}

.locale-select :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: none;
}

.locale-select :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.85);
}

/* 主体 */
.welcome-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

/* Hero 区域 */
.hero-section {
  text-align: center;
  padding: 80px 24px 60px;
  max-width: 800px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 6px 16px;
  margin-bottom: 24px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #a78bfa;
  background: rgba(167, 139, 250, 0.1);
  border: 1px solid rgba(167, 139, 250, 0.2);
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  line-height: 1.15;
  margin: 0 0 16px;
  background: linear-gradient(135deg, #fff 0%, #c4b5fd 40%, #818cf8 70%, #fff 100%);
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
  font-size: 22px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 12px;
}

.hero-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.7;
  margin: 0 auto 40px;
  max-width: 560px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.hero-cta {
  padding: 14px 36px !important;
  font-size: 17px !important;
  font-weight: 600 !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.35);
  transition: transform 0.3s ease, box-shadow 0.3s ease !important;
}

.hero-cta:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.5) !important;
}

.cta-icon {
  margin-left: 4px;
}

/* 特性区域 */
.features-section {
  padding: 40px 24px 60px;
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 48px;
  color: rgba(255, 255, 255, 0.9);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  transition: transform 0.3s ease, border-color 0.3s ease, background 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  border-color: rgba(102, 126, 234, 0.3);
  background: rgba(102, 126, 234, 0.06);
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #fff;
}

.feature-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 10px;
  color: rgba(255, 255, 255, 0.9);
}

.feature-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.6;
  margin: 0;
}

/* 底部 */
.welcome-footer {
  text-align: center;
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.welcome-footer p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
  margin: 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .hero-title {
    font-size: 38px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .section-title {
    font-size: 26px;
    margin-bottom: 32px;
  }
}

@media (max-width: 560px) {
  .hero-title {
    font-size: 30px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .hero-desc {
    font-size: 14px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .hero-cta {
    width: 100%;
  }
}
</style>
