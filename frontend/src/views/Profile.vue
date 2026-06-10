<template>
  <div class="profile-page">
    <div class="page-header">
      <h1>个人信息</h1>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：用户头像和信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card" shadow="hover">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userStore.userAvatar" @error="handleAvatarError">
              <el-icon :size="50"><UserFilled /></el-icon>
            </el-avatar>
            <h2 class="username">{{ form.username }}</h2>
            <el-tag :type="form.role === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ form.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>创建时间</span>
              <span class="info-value">{{ formatTime(form.createTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Clock /></el-icon>
              <span>更新时间</span>
              <span class="info-value">{{ formatTime(form.updateTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：编辑表单 -->
      <el-col :span="16">
        <el-card class="form-card" shadow="hover">
          <h2>编辑个人信息</h2>
          <el-divider />
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            style="max-width: 500px;"
            v-loading="loading"
          >
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled>
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱地址">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="语言偏好">
              <el-select v-model="form.language" style="width: 100%">
                <el-option label="中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
                <el-option label="日本語" value="ja-JP" />
              </el-select>
            </el-form-item>

            <el-form-item label="角色">
              <el-tag :type="form.role === 'ADMIN' ? 'danger' : ''">
                {{ form.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">
                保存修改
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码卡片 -->
        <el-card class="form-card" shadow="hover" style="margin-top: 20px;">
          <h2>修改密码</h2>
          <el-divider />
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            style="max-width: 500px;"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入当前密码"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（至少8位）"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              >
                <template #prefix>
                  <el-icon><CircleCheck /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, User, Message, Lock, Key, CircleCheck, Calendar, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { logout } from '@/api/auth'
import request from '@/utils/request'

const { t, locale } = useI18n()
const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const changingPassword = ref(false)
const formRef = ref(null)
const passwordFormRef = ref(null)

// 个人信息表单
const form = reactive({
  id: null,
  username: '',
  email: '',
  language: 'zh-CN',
  role: '',
  createTime: '',
  updateTime: ''
})

// 邮箱验证规则
const rules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ]
}

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8位', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])/, message: '密码需包含大小写字母、数字和特殊字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 头像加载错误处理
const handleAvatarError = () => {
  return false
}

// 获取用户信息
const fetchProfile = async () => {
  loading.value = true
  try {
    const response = await request.get('/users/profile')
    if (response.code === 200) {
      const user = response.data
      form.id = user.id
      form.username = user.username
      form.email = user.email || ''
      form.language = user.language || 'zh-CN'
      form.role = user.role || 'USER'
      form.createTime = user.createTime
      form.updateTime = user.updateTime
    } else {
      ElMessage.error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 保存个人信息
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const response = await request.put('/users/update-profile', {
      id: form.id,
      email: form.email,
      language: form.language
    })
    if (response.code === 200) {
      ElMessage.success('个人信息更新成功')
      // 更新 store 中的用户信息
      userStore.userInfo.email = form.email
      userStore.userInfo.language = form.language
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      // 切换语言
      if (locale.value !== form.language) {
        locale.value = form.language
        localStorage.setItem('locale', form.language)
      }
      // 刷新数据
      await fetchProfile()
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// 重置表单
const handleReset = () => {
  fetchProfile()
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  changingPassword.value = true
  try {
    const response = await request.post('/users/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    if (response.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value?.resetFields()
      // 退出登录
      setTimeout(async () => {
        try {
          await logout()
        } catch (e) {
          console.warn('后端登出调用失败', e)
        }
        userStore.logout()
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('密码修改失败，请稍后重试')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.user-card {
  text-align: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-section .username {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.info-list {
  text-align: left;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
}

.info-item .info-value {
  margin-left: auto;
  color: #909399;
  font-size: 13px;
}

.form-card h2 {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 10px;
  }
}
</style>
