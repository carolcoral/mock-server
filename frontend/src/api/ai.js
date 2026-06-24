import request from '@/utils/request'

// AI 请求专用超时时间（120秒，AI 生成响应可能需要较长时间）
const AI_TIMEOUT = 120000

/**
 * AI 生成 Mock 响应数据
 * @param {Object} params - { apiMethod, apiPath, apiName, description, count }
 * @returns {Promise}
 */
export function generateMockResponse(params) {
  return request({
    url: '/ai/generate-response',
    method: 'post',
    data: params,
    timeout: AI_TIMEOUT
  })
}

/**
 * AI 生成接口描述
 * @param {Object} params - { apiMethod, apiPath, apiName }
 * @returns {Promise}
 */
export function generateApiDescription(params) {
  return request({
    url: '/ai/generate-description',
    method: 'post',
    data: params,
    timeout: AI_TIMEOUT
  })
}

/**
 * AI 连通性验证（短超时，验证只需快速返回）
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
 * AI 生成邮件模板
 * @param {Object} params - { templateType, templateName, existingSubject, existingContent }
 * @returns {Promise}
 */
export function generateEmailTemplate(params) {
  return request({
    url: '/ai/generate-email-template',
    method: 'post',
    data: params,
    timeout: AI_TIMEOUT
  })
}

