<!--
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
-->

<template>
  <div class="ai-chat-page">
    <!-- 头部 -->
    <div class="chat-header">
      <div class="chat-header-left">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            <path d="M8 9h8" stroke-width="1.5" />
            <path d="M8 13h5" stroke-width="1.5" />
          </svg>
        </div>
        <h2>{{ $t('nav.aiChat') }}</h2>
        <el-tag v-if="modelName" size="small" type="info" effect="plain">{{ modelName }}</el-tag>
      </div>
      <div class="chat-header-right">
        <el-button size="small" text @click="clearChat" :disabled="messages.length === 0">
          <el-icon><Delete /></el-icon>
          {{ $t('aiChat.clearChat') }}
        </el-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="messagesRef">
      <!-- 空状态 -->
      <div v-if="messages.length === 0" class="chat-empty">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" width="56" height="56" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2l1.5 5.5L19 9l-5.5 1.5L12 16l-1.5-5.5L5 9l5.5-1.5z"/>
            <path d="M18 16l.75 2.25L21 19l-2.25.75L18 22l-.75-2.25L15 19l2.25-.75z"/>
            <path d="M6 18l.5 1.5L8 20l-1.5.5L6 22l-.5-1.5L4 20l1.5-.5z"/>
          </svg>
        </div>
        <h3>{{ $t('aiChat.welcomeTitle') }}</h3>
        <p>{{ $t('aiChat.welcomeDesc') }}</p>
        <div class="suggestion-chips">
          <div
            v-for="(q, idx) in suggestions"
            :key="idx"
            class="suggestion-chip"
            @click="sendMessage(q)"
          >
            <el-icon :size="14"><ChatDotRound /></el-icon>
            <span>{{ q }}</span>
          </div>
        </div>
      </div>

      <!-- 消息 -->
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="chat-message"
        :class="{ 'message-user': msg.role === 'user', 'message-assistant': msg.role === 'assistant' }"
      >
        <div class="message-avatar">
          <template v-if="msg.role === 'user'">
            <el-avatar :size="32" :src="userStore.userAvatar" />
          </template>
          <template v-else>
            <div class="ai-avatar">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2l1.5 5.5L19 9l-5.5 1.5L12 16l-1.5-5.5L5 9l5.5-1.5z"/>
                <path d="M18 16l.75 2.25L21 19l-2.25.75L18 22l-.75-2.25L15 19l2.25-.75z"/>
                <path d="M6 18l.5 1.5L8 20l-1.5.5L6 22l-.5-1.5L4 20l1.5-.5z"/>
              </svg>
            </div>
          </template>
        </div>
        <div class="message-body">
          <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
          <div class="message-actions" v-if="msg.role === 'assistant'">
            <el-button size="small" text @click="copyContent(msg.content)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 加载动画 -->
      <div v-if="loading" class="chat-message message-assistant">
        <div class="message-avatar">
          <div class="ai-avatar">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2l1.5 5.5L19 9l-5.5 1.5L12 16l-1.5-5.5L5 9l5.5-1.5z"/>
              <path d="M18 16l.75 2.25L21 19l-2.25.75L18 22l-.75-2.25L15 19l2.25-.75z"/>
              <path d="M6 18l.5 1.5L8 20l-1.5.5L6 22l-.5-1.5L4 20l1.5-.5z"/>
            </svg>
          </div>
        </div>
        <div class="message-body">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <div class="chat-input-wrapper">
        <textarea
          v-model="inputText"
          class="chat-textarea"
          :placeholder="$t('aiChat.inputPlaceholder')"
          :disabled="loading"
          rows="1"
          ref="textareaRef"
          @input="autoResize"
          @keydown.enter.exact.prevent="sendMessage(inputText)"
        ></textarea>
        <div class="chat-input-footer">
          <span class="input-hint">{{ $t('aiChat.enterHint') }}，Shift+Enter 换行</span>
          <el-button
            type="primary"
            :disabled="!inputText.trim() || loading"
            :loading="loading"
            size="small"
            @click="sendMessage(inputText)"
          >
            <el-icon v-if="!loading"><Position /></el-icon>
            <span>{{ loading ? $t('aiChat.thinking') : '' }}</span>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { marked } from 'marked'
import {
  Delete, CopyDocument, Position, ChatDotRound
} from '@element-plus/icons-vue'

const { t } = useI18n()
const userStore = useUserStore()

// 按用户隔离的对话历史 key
const CHAT_STORAGE_PREFIX = 'ai_chat_history_'

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const textareaRef = ref(null)
const modelName = ref('')

// 建议问题（从后端获取，后端基于 README + CHANGELOG 生成并缓存）
const suggestions = ref([])

// 获取当前用户的 localStorage key
const getStorageKey = () => {
  const uid = userStore.userId || userStore.username || 'unknown'
  return CHAT_STORAGE_PREFIX + uid
}

// 保存对话历史到 localStorage（按用户隔离）
const saveHistory = () => {
  try {
    const key = getStorageKey()
    localStorage.setItem(key, JSON.stringify(messages.value))
  } catch (e) {
    // localStorage 可能满
  }
}

// 从 localStorage 恢复对话历史
const loadHistory = () => {
  try {
    const key = getStorageKey()
    const raw = localStorage.getItem(key)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) {
        messages.value = parsed
      }
    }
  } catch (e) {
    // 忽略解析错误
  }
}

// 渲染 Markdown
const renderMarkdown = (text) => {
  if (!text) return ''
  try {
    return marked(text)
  } catch {
    return text
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 复制内容
const copyContent = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('aiChat.copySuccess'))
  } catch {
    ElMessage.error(t('aiChat.copyFailed'))
  }
}

// textarea 自动调整高度
const autoResize = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 120) + 'px'
  }
}

// 获取建议问题（后端基于 README + CHANGELOG 生成并缓存）
const fetchSuggestions = async () => {
  try {
    const response = await request.get('/ai/chat-suggestions')
    if (response.code === 200 && response.data && Array.isArray(response.data)) {
      suggestions.value = response.data
    }
  } catch {
    // 静默失败，使用默认值
    suggestions.value = [
      t('aiChat.suggest1'),
      t('aiChat.suggest2'),
      t('aiChat.suggest3'),
      t('aiChat.suggest4')
    ]
  }
}

// 获取模型名称
const fetchModelName = async () => {
  try {
    const response = await request.get('/ai-config/enabled')
    if (response.code === 200 && response.data) {
      modelName.value = response.data.defaultModel || ''
    }
  } catch {
    // 静默失败
  }
}

// 发送消息（流式 SSE）
const sendMessage = async (text) => {
  const content = typeof text === 'string' ? text.trim() : inputText.value.trim()
  if (!content || loading.value) return

  inputText.value = ''
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }

  // 添加用户消息
  messages.value.push({ role: 'user', content })
  saveHistory()
  await scrollToBottom()

  loading.value = true

  // 先插入一条空的 assistant 消息，后续逐 token 追加
  const assistantIdx = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  await scrollToBottom()

  try {
    const apiMessages = messages.value
      .filter(m => m.role !== 'assistant' || m.content !== '') // 过滤空的 assistant
      .map(m => ({ role: m.role, content: m.content }))
    // 修正：将刚插入的空 assistant 也计入 context（content 为空，AI 也能理解）
    // 实际上我们只需要 user + 之前的 assistant，但当前这条空 assistant 不应发送
    const reqMessages = messages.value
      .slice(0, assistantIdx) // 不包含当前空的 assistant
      .map(m => ({ role: m.role, content: m.content }))

    const token = userStore.token
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

    const response = await fetch(`${baseURL}/ai/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({ messages: reqMessages })
    })

    if (!response.ok) {
      // 尝试读取错误信息
      const errorText = await response.text()
      let errorMsg = t('aiChat.networkError')
      // 从 SSE 数据中提取错误
      const errorMatch = errorText.match(/\[ERROR\]\s*(.+)/)
      if (errorMatch) {
        errorMsg = errorMatch[1]
      } else if (response.status === 403) {
        errorMsg = '没有权限访问'
      }
      throw new Error(errorMsg)
    }

    // 流式读取 SSE
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 按行解析 SSE
      const lines = buffer.split('\n')
      // 保留最后一个可能不完整的行
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.substring(6).trim()
          if (data === '[DONE]') {
            continue
          }
          if (data.startsWith('[ERROR]')) {
            const errMsg = data.substring(8).trim()
            throw new Error(errMsg || t('aiChat.error'))
          }
          try {
            const parsed = JSON.parse(data)
            const delta = parsed?.choices?.[0]?.delta?.content
            if (delta) {
              messages.value[assistantIdx].content += delta
              await scrollToBottom()
            }
          } catch (e) {
            // 忽略非 JSON 行
          }
        }
      }
    }

    // 处理 buffer 中剩余的数据
    if (buffer.startsWith('data: ')) {
      const data = buffer.substring(6).trim()
      if (data !== '[DONE]' && !data.startsWith('[ERROR]')) {
        try {
          const parsed = JSON.parse(data)
          const delta = parsed?.choices?.[0]?.delta?.content
          if (delta) {
            messages.value[assistantIdx].content += delta
            await scrollToBottom()
          }
        } catch (e) {}
      }
    }

    // 如果 AI 返回空内容（异常情况）
    if (!messages.value[assistantIdx].content) {
      messages.value[assistantIdx].content = t('aiChat.emptyReply')
    }
    saveHistory()
  } catch (error) {
    console.error('AI 流式对话失败:', error)
    // 移除空的 assistant 消息
    if (messages.value[assistantIdx] && !messages.value[assistantIdx].content) {
      messages.value.splice(assistantIdx, 1)
    }
    ElMessage.error(error.message || t('aiChat.networkError'))
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

// 清空对话
const clearChat = () => {
  messages.value = []
  inputText.value = ''
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }
  try {
    localStorage.removeItem(getStorageKey())
  } catch (e) {}
}

// 监听消息变化自动滚动
watch(() => messages.value.length, () => {
  scrollToBottom()
})

onMounted(() => {
  fetchModelName()
  fetchSuggestions()
  loadHistory()
  // 恢复后滚动到底部
  if (messages.value.length > 0) {
    scrollToBottom()
  }
})

// 组件卸载前保存（切换路由时）
onBeforeUnmount(() => {
  if (messages.value.length > 0) {
    saveHistory()
  }
})
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  flex-direction: column;
  /* 使用 100% 高度自适应，由父容器 el-main 决定实际高度（含页脚时自动缩小） */
  height: 100%;
  max-width: 800px;
  margin: 0 auto;
}

/* 头部 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0 10px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.header-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.chat-header-left h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
  scroll-behavior: smooth;
}

.chat-messages::-webkit-scrollbar {
  width: 5px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

/* 空状态 */
.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 280px;
  text-align: center;
}

.empty-icon {
  color: #a0a4b0;
  margin-bottom: 16px;
  opacity: 0.5;
}

.chat-empty h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.chat-empty p {
  margin: 0 0 20px;
  font-size: 13px;
  color: #909399;
  max-width: 360px;
  line-height: 1.6;
}

.suggestion-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  max-width: 520px;
}

.suggestion-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 20px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.suggestion-chip:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
  color: #409EFF;
}

/* 消息项 */
.chat-message {
  display: flex;
  gap: 10px;
  padding: 10px 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-assistant {
  background: #f7f8fa;
  border-radius: 10px;
  margin: 2px 0;
}

.message-avatar {
  flex-shrink: 0;
  padding-top: 2px;
}

.ai-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.message-body {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.message-content {
  font-size: 14px;
  line-height: 1.75;
  color: #303133;
  word-break: break-word;
}

/* Markdown 样式 */
.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4) {
  margin: 14px 0 6px;
  font-weight: 600;
  line-height: 1.4;
}

.message-content :deep(h1) { font-size: 19px; }
.message-content :deep(h2) { font-size: 16px; }
.message-content :deep(h3) { font-size: 14px; }

.message-content :deep(p) {
  margin: 4px 0;
}

.message-content :deep(p:first-child) {
  margin-top: 0;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  padding-left: 20px;
  margin: 4px 0;
}

.message-content :deep(li) {
  margin: 2px 0;
}

.message-content :deep(code) {
  background: #e8eaed;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 13px;
  font-family: 'SF Mono', 'Menlo', 'Consolas', monospace;
  color: #d63384;
}

.message-content :deep(pre) {
  background: #282a36;
  border-radius: 8px;
  padding: 14px 16px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-content :deep(pre code) {
  background: none;
  color: #f8f8f2;
  padding: 0;
  font-size: 13px;
}

.message-content :deep(blockquote) {
  border-left: 3px solid #667eea;
  padding: 2px 0 2px 12px;
  margin: 6px 0;
  color: #606266;
  background: rgba(102, 126, 234, 0.04);
  border-radius: 0 4px 4px 0;
}

.message-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 6px 0;
  font-size: 13px;
}

.message-content :deep(table th),
.message-content :deep(table td) {
  border: 1px solid #e4e7ed;
  padding: 6px 12px;
  text-align: left;
}

.message-content :deep(table th) {
  background: #f5f7fa;
  font-weight: 600;
}

.message-content :deep(hr) {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 12px 0;
}

.message-content :deep(a) {
  color: #409EFF;
  text-decoration: none;
}

.message-content :deep(a:hover) {
  text-decoration: underline;
}

.message-actions {
  margin-top: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-message:hover .message-actions {
  opacity: 1;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 6px 0;
}

.typing-indicator span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #b0b8c8;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.5); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* 输入区域 */
.chat-input-area {
  flex-shrink: 0;
  padding: 10px 0 0;
  border-top: 1px solid #ebeef5;
}

.chat-input-wrapper {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 10px 12px;
  transition: all 0.2s ease;
}

.chat-input-wrapper:focus-within {
  border-color: #409EFF;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.08);
}

.chat-textarea {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  resize: none;
  font-family: inherit;
  overflow-y: auto;
  min-height: 24px;
}

.chat-textarea::placeholder {
  color: #c0c4cc;
}

.chat-textarea:disabled {
  opacity: 0.6;
}

.chat-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 6px;
  margin-top: 2px;
  border-top: 1px solid transparent;
}

.chat-input-wrapper:focus-within .chat-input-footer {
  border-top-color: #ebeef5;
}

.input-hint {
  font-size: 11px;
  color: #c0c4cc;
}

/* 响应式 */
@media (max-width: 768px) {
  .ai-chat-page {
    max-width: none;
    height: 100%;
  }

  .chat-message {
    padding: 10px 12px;
  }

  .suggestion-chip {
    font-size: 12px;
    padding: 6px 12px;
  }
}
</style>
