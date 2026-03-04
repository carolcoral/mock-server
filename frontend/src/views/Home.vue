<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon project-icon">
            <Folder :width="'1.5em'" :height="'1.5em'" />
          </div>
          <div class="stat-content">
            <h3>项目总数</h3>
            <p>{{ loading ? '-' : stats.projectCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon api-icon">
            <Connection :width="'1.5em'" :height="'1.5em'" />
          </div>
          <div class="stat-content">
            <h3>接口总数</h3>
            <p>{{ loading ? '-' : stats.apiCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon user-icon">
            <User :width="'1.5em'" :height="'1.5em'" />
          </div>
          <div class="stat-content">
            <h3>用户总数</h3>
            <p>{{ loading ? '-' : stats.userCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon request-icon">
            <Position :width="'1.5em'" :height="'1.5em'" />
          </div>
          <div class="stat-content">
            <h3>今日请求</h3>
            <p>{{ loading ? '-' : stats.requestCount }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card title="快速开始">
          <template #header>
            <span>快速开始</span>
          </template>
          <div class="quick-start">
            <el-button type="primary" @click="$router.push('/projects')" style="margin: 5px;">
              <Folder :width="'1.5em'" :height="'1.5em'" />
              创建项目
            </el-button>
            <el-button type="success" @click="$router.push('/apis')" style="margin: 5px;">
              <Connection :width="'1.5em'" :height="'1.5em'" />
              创建接口
            </el-button>
            <el-button @click="showSwagger" style="margin: 5px;">
              <Document :width="'1.5em'" :height="'1.5em'" />
              Swagger文档
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="系统信息">
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="system-info">
            <p><strong>系统版本:</strong> v1.0.0</p>
            <p><strong>作者:</strong> carolcoral</p>
            <p><strong>GitHub:</strong> <a href="https://github.com/carolcoral" target="_blank">github.com/carolcoral</a></p>
            <p><strong>技术栈:</strong> Vue 3 + Spring Boot + SQLite</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import service from '@/utils/request'
import axios from 'axios'
import {
  Folder,
  Connection,
  User,
  Position,
  Document
} from '@element-plus/icons-vue'

// 统计数据
const stats = ref({
  projectCount: 0,
  apiCount: 0,
  userCount: 0,
  requestCount: 0
})

const loading = ref(true)

  // 显示Swagger文档
  const showSwagger = async () => {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录后再访问Swagger文档')
      return
    }

    try {
      // 调用Swagger自动登录接口（后端验证用户token后返回Swagger token）
      const response = await axios.post(
        '/api/auth/swagger-auto-login',
        {},
        {
          headers: {
            'Authorization': `Bearer ${userStore.token}`
          }
        }
      )

      if (response.data.code === 200) {
        // 获取到Swagger专用token
        const swaggerToken = response.data.data.token
        
        // 打开Swagger页面
        const swaggerUrl = '/api/swagger-ui.html'
        const swaggerWindow = window.open(swaggerUrl, '_blank')
        
        // 尝试自动填充token到Swagger UI（通过localStorage）
        setTimeout(() => {
          try {
            if (swaggerWindow && !swaggerWindow.closed) {
              // 将token存储到Swagger页面的localStorage
              swaggerWindow.localStorage.setItem('swagger-auth-token', swaggerToken)
              console.log('Swagger token已存储到localStorage')
              
              // 尝试自动点击Authorize按钮并填充token
              swaggerWindow.addEventListener('load', () => {
                try {
                  // 查找Authorize按钮并点击
                  const authorizeBtn = swaggerWindow.document.querySelector('.auth-wrapper .authorize-btn')
                  if (authorizeBtn) {
                    authorizeBtn.click()
                    
                    // 等待弹窗出现后填充token
                    setTimeout(() => {
                      const tokenInput = swaggerWindow.document.querySelector('input[name="bearer"]')
                      if (tokenInput) {
                        tokenInput.value = swaggerToken
                      }
                    }, 500)
                  }
                } catch (e) {
                  console.log('无法自动完成Swagger认证，请手动操作')
                }
              })
            }
          } catch (e) {
            console.log('无法自动填充token到Swagger UI，请手动输入')
          }
        }, 1000)
        
        ElMessage.success('正在跳转到Swagger文档...')
      } else {
        ElMessage.error('Swagger自动登录失败：' + response.data.message)
      }
    } catch (error) {
      console.error('Swagger自动登录失败', error)
      if (error.response && error.response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        userStore.logout()
        window.location.href = '/login'
      } else {
        ElMessage.error('自动登录失败，请稍后重试')
      }
    }
  }

// 页面加载时获取统计数据
onMounted(() => {
  fetchRealStats()
})

// 获取真实统计数据
const fetchRealStats = async () => {
  loading.value = true
  try {
    const response = await service.get('/dashboard/stats?includeTodayRequests=true')
    if (response.code === 200) {
      stats.value = {
        projectCount: response.data.projectCount || 0,
        apiCount: response.data.apiCount || 0,
        userCount: response.data.userCount || 0,
        requestCount: response.data.requestCount || 0
      }
    } else {
      ElMessage.error('获取统计数据失败')
      setDefaultStats()
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
    ElMessage.error('获取统计数据失败，请稍后重试')
    setDefaultStats()
  } finally {
    loading.value = false
  }
}

// 设置默认值
const setDefaultStats = () => {
  stats.value = {
    projectCount: 0,
    apiCount: 0,
    userCount: 0,
    requestCount: 0
  }
}
</script>

<style scoped>
.home {
  padding: 20px;
}

.stat-card {
  height: 120px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  padding: 0 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  flex-shrink: 0;
  float: left;
}

.stat-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.project-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.api-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.user-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.request-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-content {
  flex: 1;
  text-align: left;
}

.stat-content h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #909399;
  font-weight: normal;
  letter-spacing: 0.5px;
}

.stat-content p {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.quick-start {
  padding: 10px;
}

.system-info {
  padding: 10px;
  line-height: 2;
}

.system-info p {
  margin: 5px 0;
}

.system-info a {
  color: #409EFF;
  text-decoration: none;
}

.system-info a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .home {
    padding: 10px;
  }
  
  .stat-card {
    height: 100px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
  
  .stat-content p {
    font-size: 20px;
  }
}
</style>
