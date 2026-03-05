import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value.role === 'ADMIN')
  const username = computed(() => userInfo.value.username || '')
  const userAvatar = computed(() => {
    try {
      // 使用 cravatar.cn 生成头像
      // 如果没有邮箱，使用用户名生成虚拟邮箱
      const emailForAvatar = userInfo.value.email || `${userInfo.value.username || 'user'}@cravatar.cn`
      const avatarUrl = `https://cravatar.cn/avatar/${emailForAvatar}?s=200&r=g&d=retro`
      return avatarUrl
    } catch (error) {
      console.warn('生成头像URL失败:', error)
      return ''
    }
  })

  // 登录
  const login = async (username, password) => {
    try {
      const response = await loginApi({ username, password })
      if (response.code === 200) {
        const { token: userToken, userId, username: name, role, email } = response.data

        // 保存token
        token.value = userToken
        localStorage.setItem('token', userToken)

        // 保存用户信息
        userInfo.value = { id: userId, username: name, role, email }
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
