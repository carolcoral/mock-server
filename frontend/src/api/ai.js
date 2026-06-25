/*
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

import request from '@/utils/request'

// AI 请求专用超时时间（180秒，AI 生成响应实际约100-130秒）
const AI_TIMEOUT = 180000

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

/**
 * AI 生成代码模板（CustomResponseTransformer Java 源码）
 * @param {Object} params - { apiMethod, apiPath, apiName, description, transformerType, existingSourceCode }
 * @returns {Promise}
 */
export function generateCodeTemplate(params) {
  return request({
    url: '/ai/generate-code-template',
    method: 'post',
    data: params,
    timeout: AI_TIMEOUT
  })
}

