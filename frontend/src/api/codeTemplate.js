import request from '@/utils/request'

/**
 * 获取用户可访问的自定义代码模板列表（支持服务器端过滤）
 * 系统管理员：所有项目的模板
 * 普通用户：所属项目的模板
 * @param {Object} params - 可选过滤参数 { name, projectId, enabled }
 */
export function getAccessibleTemplates(params = {}) {
  return request({
    url: '/code-templates',
    method: 'get',
    params
  })
}

/**
 * 根据项目ID获取模板列表
 * @param {Number} projectId 项目ID
 */
export function getTemplatesByProjectId(projectId) {
  return request({
    url: `/code-templates/project/${projectId}`,
    method: 'get'
  })
}

/**
 * 根据项目ID获取已启用的模板列表（用于下拉选择）
 * @param {Number} projectId 项目ID
 */
export function getEnabledTemplatesByProjectId(projectId) {
  return request({
    url: `/code-templates/project/${projectId}/enabled`,
    method: 'get'
  })
}

/**
 * 根据ID获取模板详情
 * @param {Number} id 模板ID
 */
export function getTemplateById(id) {
  return request({
    url: `/code-templates/${id}`,
    method: 'get'
  })
}

/**
 * 创建自定义代码模板
 * @param {Object} data 模板数据 { name, description, sourceCode, enabled, project: { id } }
 */
export function createTemplate(data) {
  return request({
    url: '/code-templates',
    method: 'post',
    data
  })
}

/**
 * 更新自定义代码模板
 * @param {Object} data 模板数据
 */
export function updateTemplate(data) {
  return request({
    url: '/code-templates',
    method: 'put',
    data
  })
}

/**
 * 删除自定义代码模板
 * @param {Number} id 模板ID
 */
export function deleteTemplate(id) {
  return request({
    url: `/code-templates/${id}`,
    method: 'delete'
  })
}

/**
 * 编译验证模板源码
 * @param {Object} data { sourceCode, templateId }
 */
export function validateTemplateSourceCode(data) {
  return request({
    url: '/code-templates/validate',
    method: 'post',
    data
  })
}
