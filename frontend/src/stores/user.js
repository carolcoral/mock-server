import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { login as loginApi } from '@/api/auth'

// SHA256 哈希工具函数
async function sha256(message) {
  const msgBuffer = new TextEncoder().encode(message)
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 头像 URL（异步生成 SHA256 哈希后更新）
  const userAvatar = ref('/default-avatar.png')

  const updateAvatarUrl = async () => {
    // 没有用户信息时使用默认头像
    if (!userInfo.value.email && !userInfo.value.username) {
      userAvatar.value = '/default-avatar.png'
      return
    }
    try {
      const email = userInfo.value.email || `${userInfo.value.username || 'user'}@cravatar.cn`
      const hash = await sha256(email.toLowerCase().trim())
      userAvatar.value = `https://cn.cravatar.com/avatar/${hash}?s=200&r=g`
    } catch (error) {
      console.warn('生成头像URL失败:', error)
      userAvatar.value = '/default-avatar.png'
    }
  }

  // 监听用户信息变更，自动更新头像
  watch(userInfo, () => {
    updateAvatarUrl()
  }, { immediate: true, deep: true })

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value.role === 'ADMIN')
  const username = computed(() => userInfo.value.username || '')

  // 登录
  const login = async (username, password) => {
    try {
      const response = await loginApi({ username, password })
      if (response.code === 200) {
        const { token: userToken, userId, username: name, role, email, language } = response.data

        // 保存token
        token.value = userToken
        localStorage.setItem('token', userToken)

        // 保存用户信息
        userInfo.value = { id: userId, username: name, role, email, language }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

        return { success: true }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 设置token
  const setToken = (userToken) => {
    token.value = userToken
    localStorage.setItem('token', userToken)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    username,
    userAvatar,
    login,
    logout,
    setToken
  }
})
