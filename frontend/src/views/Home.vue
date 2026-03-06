<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="colSpan">
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
      <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="colSpan">
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
      <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="colSpan">
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
      <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="colSpan">
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
      <el-col :xs="12" :sm="12" :md="12" :lg="6" :xl="colSpan">
        <el-card class="stat-card">
          <div class="stat-icon total-request-icon">
            <DataLine :width="'1.5em'" :height="'1.5em'" />
          </div>
          <div class="stat-content">
            <h3>历史请求</h3>
            <p>{{ loading ? '-' : stats.totalRequestCount }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
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
            <el-button @click="$router.push('/guide')" style="margin: 5px;">
              <Document :width="'1.5em'" :height="'1.5em'" />
              使用说明
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统公告</span>
          </template>
          <div class="announcement" v-if="announcement">
            <div class="announcement-content" v-html="renderedContent"></div>
          </div>
          <div v-else class="no-announcement">
            <el-empty description="暂无公告" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import {
  Folder,
  Connection,
  User,
  Position,
  Document,
  DataLine
} from '@element-plus/icons-vue'
import { marked } from 'marked'

// 统计数据
const stats = ref({
  projectCount: 0,
  apiCount: 0,
  userCount: 0,
  requestCount: 0,
  totalRequestCount: 0
})

// 系统公告
const announcement = ref(null)

const loading = ref(true)

// 计算每个统计项的宽度（当页面宽度大于750px时，5个统计项在一行显示）
const colSpan = computed(() => {
  // 获取页面宽度
  const width = window.innerWidth || 1920

  // 如果页面宽度大于750px，每个统计项占20%（5个*20%=100%），在一行显示
  if (width > 750) {
    return 24 / 5 // el-col 的 span 是 24 的比例，5个统计项每个占 4.8
  }

  // 否则使用默认的响应式布局
  return 12
})

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (announcement.value && announcement.value.content) {
    return marked(announcement.value.content)
  }
  return ''
})

  // 显示Swagger文档
  const showSwagger = async () => {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录后再访问Swagger文档')
      return
    }

    try {
      // 调用Swagger自动登录接口（后端验证用户token后返回Swagger token）
      const response = await request({
        url: '/auth/swagger-auto-login',
        method: 'post',
        data: {}
      })

      if (response.code === 200) {
        // 获取到Swagger专用token
        const swaggerToken = response.data.token
        
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
  fetchAnnouncement()
})

// 获取真实统计数据
const fetchRealStats = async () => {
  loading.value = true
  try {
    const response = await request.get('/dashboard/stats?includeTodayRequests=true')
      if (response.code === 200) {
      stats.value = {
        projectCount: response.data.projectCount || 0,
        apiCount: response.data.apiCount || 0,
        userCount: response.data.userCount || 0,
        requestCount: response.data.requestCount || 0,
        totalRequestCount: response.data.totalRequestCount || 0
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
    requestCount: 0,
    totalRequestCount: 0
  }
}

// 获取系统公告
const fetchAnnouncement = async () => {
  try {
    const response = await request.get('/dashboard/announcement')
    if (response.code === 200 && response.data) {
      announcement.value = response.data
    }
  } catch (error) {
    console.error('获取系统公告失败', error)
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

.total-request-icon {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
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

.announcement {
  padding: 0;
}

.announcement-content {
  color: #606266;
  line-height: 1.8;
  max-height: 20vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px;
  word-wrap: break-word;
  word-break: break-word;
}

.announcement-content h1,
.announcement-content h2,
.announcement-content h3,
.announcement-content h4,
.announcement-content h5,
.announcement-content h6 {
  margin: 15px 0 10px 0;
  color: #303133;
}

.announcement-content p {
  margin: 10px 0;
}

.announcement-content ul,
.announcement-content ol {
  margin: 10px 0;
  padding-left: 20px;
}

.announcement-content li {
  margin: 5px 0;
}

.announcement-content code {
  background-color: #f5f5f5;
  padding: 2px 5px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.announcement-content pre {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  margin: 15px 0;
}

.announcement-content pre code {
  background-color: transparent;
  padding: 0;
}

.announcement-content blockquote {
  border-left: 4px solid #409EFF;
  padding-left: 15px;
  margin: 15px 0;
  color: #909399;
}

.no-announcement {
  padding: 20px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

@media (max-width: 768px) {
  .home {
    padding: 10px;
  }

  .stat-card {
    height: 100px;
    margin-top: 10px;
    margin-bottom: 15px;
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

@media (max-width: 768px) {
  .home {
    padding: 10px;
  }
}

/* 1200px - 1599px: 每个统计项最大宽度25% */
@media only screen and (min-width: 1200px) and (max-width: 1599px) {
  .el-col-lg-6,
  .el-col-lg-6.is-guttered {
    max-width: 25%;
    margin-top: 10px;
  }
}

/* 1600px及以上: 每个统计项最大宽度20% */
@media only screen and (min-width: 1600px) {
  .el-col-lg-6,
  .el-col-lg-6.is-guttered {
    max-width: 20%;
  }
}
</style>
