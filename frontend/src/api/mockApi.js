import request from '@/utils/request'

/**
 * 获取接口列表
 * @returns {Promise}
 */
export function getMockApiList() {
  return request({
    url: '/mock-apis',
    method: 'get'
  })
}

/**
 * 根据项目ID获取接口列表
 * @param {Number} projectId 项目ID
 * @returns {Promise}
 */
export function getMockApisByProjectId(projectId) {
  return request({
    url: `/mock-apis/project/${projectId}`,
    method: 'get'
  })
}

/**
 * 根据ID获取接口
 * @param {Number} id 接口ID
 * @returns {Promise}
 */
export function getMockApiById(id) {
  return request({
    url: `/mock-apis/${id}`,
    method: 'get'
  })
}

/**
 * 创建接口
 * @param {Object} data 接口数据
 * @returns {Promise}
 */
export function createMockApi(data) {
  return request({
    url: '/mock-apis',
    method: 'post',
    data
  })
}

/**
 * 更新接口
 * @param {Object} data 接口数据
 * @returns {Promise}
 */
export function updateMockApi(data) {
  return request({
    url: '/mock-apis',
    method: 'put',
    data
  })
}

/**
 * 删除接口
 * @param {Number} id 接口ID
 * @returns {Promise}
 */
export function deleteMockApi(id) {
  return request({
    url: `/mock-apis/${id}`,
    method: 'delete'
  })
}

/**
 * 切换接口状态
 * @param {Number} id 接口ID
 * @returns {Promise}
 */
export function toggleApiStatus(id) {
  return request({
    url: `/mock-apis/${id}/toggle`,
    method: 'put'
  })
}

/**
 * 添加接口响应
 * @param {Number} apiId 接口ID
 * @param {Object} data 响应数据
 * @returns {Promise}
 */
export function addApiResponse(apiId, data) {
  return request({
    url: `/mock-apis/${apiId}/responses`,
    method: 'post',
    data
  })
}

/**
 * 更新接口响应
 * @param {Object} data 响应数据
 * @returns {Promise}
 */
export function updateApiResponse(data) {
  return request({
    url: '/mock-apis/responses',
    method: 'put',
    data
  })
}

/**
 * 删除接口响应
 * @param {Number} responseId 响应ID
 * @returns {Promise}
 */
export function deleteApiResponse(responseId) {
  return request({
    url: `/mock-apis/responses/${responseId}`,
    method: 'delete'
  })
}
