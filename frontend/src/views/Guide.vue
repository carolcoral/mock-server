<template>
  <div class="guide">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>使用说明</span>
        </div>
      </template>
      <div class="guide-content" v-if="loading">
        <el-skeleton :rows="10" animated />
      </div>
      <div class="guide-content" v-else v-html="renderedContent"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { marked } from 'marked'
import request from '@/utils/request'

const loading = ref(true)
const renderedContent = ref('')

// 获取使用说明文档
const fetchGuide = async () => {
  try {
    const response = await fetch('/USER_GUIDE.md')
    const content = await response.text()
    renderedContent.value = marked(content)
    loading.value = false
  } catch (error) {
    console.error('获取使用说明失败:', error)
    renderedContent.value = '<p class="error">加载使用说明失败</p>'
    loading.value = false
  }
}

onMounted(() => {
  fetchGuide()
})
</script>

<style scoped>
.guide {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
}

.guide-content {
  padding: 20px 0;
  line-height: 1.8;
  color: #303133;
}

.error {
  color: #f56c6c;
  text-align: center;
  padding: 40px 0;
}

/* Markdown 样式 */
.guide-content :deep(h1) {
  font-size: 32px;
  font-weight: 600;
  margin: 30px 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #e4e7ed;
  color: #303133;
}

.guide-content :deep(h2) {
  font-size: 24px;
  font-weight: 600;
  margin: 25px 0 15px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
  color: #303133;
}

.guide-content :deep(h3) {
  font-size: 20px;
  font-weight: 600;
  margin: 20px 0 12px 0;
  color: #303133;
}

.guide-content :deep(h4) {
  font-size: 18px;
  font-weight: 600;
  margin: 18px 0 10px 0;
  color: #303133;
}

.guide-content :deep(p) {
  margin: 12px 0;
  line-height: 1.8;
}

.guide-content :deep(ul),
.guide-content :deep(ol) {
  margin: 12px 0;
  padding-left: 30px;
}

.guide-content :deep(li) {
  margin: 8px 0;
  line-height: 1.8;
}

.guide-content :deep(code) {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  color: #e83e8c;
}

.guide-content :deep(pre) {
  background-color: #282c34;
  color: #abb2bf;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  margin: 15px 0;
  line-height: 1.6;
}

.guide-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: #abb2bf;
  font-size: 14px;
}

.guide-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 15px;
  margin: 15px 0;
  color: #606266;
  background-color: #f4f4f5;
  padding: 10px 15px;
  border-radius: 3px;
}

.guide-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.guide-content :deep(table th),
.guide-content :deep(table td) {
  border: 1px solid #e4e7ed;
  padding: 10px;
  text-align: left;
}

.guide-content :deep(table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

.guide-content :deep(table tr:hover) {
  background-color: #f5f7fa;
}

.guide-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.guide-content :deep(a:hover) {
  text-decoration: underline;
}

.guide-content :deep(strong) {
  font-weight: 600;
  color: #303133;
}

.guide-content :deep(hr) {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 30px 0;
}
</style>
