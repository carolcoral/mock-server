<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon project-icon">
            <Folder />
          </div>
          <div class="stat-content">
            <h3>项目总数</h3>
            <p>{{ stats.projectCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon api-icon">
            <Connection />
          </div>
          <div class="stat-content">
            <h3>接口总数</h3>
            <p>{{ stats.apiCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon user-icon">
            <User />
          </div>
          <div class="stat-content">
            <h3>用户总数</h3>
            <p>{{ stats.userCount }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon request-icon">
            <Position />
          </div>
          <div class="stat-content">
            <h3>今日请求</h3>
            <p>{{ stats.requestCount }}</p>
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
              <Folder />
              创建项目
            </el-button>
            <el-button type="success" @click="$router.push('/apis')" style="margin: 5px;">
              <Connection />
              创建接口
            </el-button>
            <el-button @click="showSwagger" style="margin: 5px;">
              <Document />
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

// 显示Swagger文档
const showSwagger = () => {
  window.open('http://localhost:8080/api/swagger-ui.html', '_blank')
}

// 页面加载时获取统计数据
onMounted(() => {
  // 这里可以调用API获取真实数据
  stats.value = {
    projectCount: 5,
    apiCount: 25,
    userCount: 3,
    requestCount: 1250
  }
})
</script>

<style scoped>
.home {
  padding: 20px;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
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

.stat-content h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.stat-content p {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
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
