/*
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

// ==================== 流式 SSE 通用请求 ====================

/**
 * 发起流式 SSE 请求并累积结果
 * @param {string} url - API 路径
 * @param {Object} body - 请求体
 * @param {Function} onProgress - 流式进度回调 (accumulatedText)
 * @returns {Promise<string>} 完整的生成文本
 */
export async function fetchStream(url, body, onProgress) {
  const userStore = useUserStore()
  const token = userStore.token
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

  const response = await fetch(`${baseURL}${url}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(body)
  })

  if (!response.ok) {
    const text = await response.text()
    const errMatch = text.match(/\[ERROR\]\s*(.+)/)
    throw new Error(errMatch ? errMatch[1] : 'AI 请求失败')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let fullText = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      if (line.startsWith('data: ')) {
        const data = line.substring(6).trim()
        if (data === '[DONE]') continue
        if (data.startsWith('[ERROR]')) {
          throw new Error(data.substring(8).trim() || 'AI 生成失败')
        }
        try {
          const parsed = JSON.parse(data)
          const delta = parsed?.choices?.[0]?.delta?.content
          if (delta) {
            fullText += delta
            if (onProgress) onProgress(fullText)
          }
        } catch (e) {
          // 忽略非 JSON 行
        }
      }
    }
  }

  return fullText
}

// ==================== AI 生成 API ====================

/**
 * AI 生成 Mock 响应数据
 * @param {Object} params - { apiMethod, apiPath, apiName, description, count }
 * @returns {Promise}
 */
export function generateMockResponse(params) {
  return request({
    url: '/ai/generate-response',
    method: 'post',
    data: params
  })
}

/**
 * AI 生成接口描述（流式）
 * @param {Object} params - { apiMethod, apiPath, apiName }
 * @returns {Promise<string>} 生成的描述文本
 */
export function generateApiDescriptionStream(params) {
  return fetchStream('/ai/generate-description/stream', params)
}

/**
 * AI 生成接口描述（非流式，保留兼容）
 */
export function generateApiDescription(params) {
  return request({
    url: '/ai/generate-description',
    method: 'post',
    data: params
  })
}

/**
 * AI 连通性验证（短超时）
 * @param {Object} params - { apiUrl, apiKey, defaultModel }
 * @returns {Promise}
 */
export function testAiConnectivity(params) {
  return request({
    url: '/ai-config/test-connectivity',
    method: 'post',
    data: params,
    timeout: 20000
  })
}

/**
 * AI 生成邮件模板（流式）
 * @param {Object} params - { templateType, templateName, existingSubject, existingContent }
 * @returns {Promise<string>} 完整的生成文本
 */
export function generateEmailTemplateStream(params) {
  return fetchStream('/ai/generate-email-template/stream', params)
}

/**
 * AI 生成邮件模板（非流式，保留兼容）
 */
export function generateEmailTemplate(params) {
  return request({
    url: '/ai/generate-email-template',
    method: 'post',
    data: params
  })
}

/**
 * AI 生成代码模板（流式）
 * @param {Object} params - { apiMethod, apiPath, apiName, description, transformerType, existingSourceCode }
 * @returns {Promise<string>} 完整的生成文本
 */
export function generateCodeTemplateStream(params) {
  return fetchStream('/ai/generate-code-template/stream', params)
}

/**
 * AI 生成代码模板（非流式，保留兼容）
 */
export function generateCodeTemplate(params) {
  return request({
    url: '/ai/generate-code-template',
    method: 'post',
    data: params
  })
}

