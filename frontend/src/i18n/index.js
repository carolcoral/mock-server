import { createI18n } from 'vue-i18n'
import zhCN from '../locales/zh-CN'
import enUS from '../locales/en-US'
import jaJP from '../locales/ja-JP'

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'ja-JP': jaJP
}

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages,
  globalInjection: true
})

export default i18n
