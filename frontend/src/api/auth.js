import request from '@/utils/request'

/**
 * 登录
 * @param {Object} data 登录数据
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * Swagger登录
 * @param {Object} data 登录数据
 * @returns {Promise}
 */
export function swaggerLogin(data) {
  return request({
    url: '/auth/swagger-login',
    method: 'post',
    data
  })
}

/**
 * Swagger自动登录（已登录用户调用）
 * @returns {Promise}
 */
export function swaggerAutoLogin() {
  return request({
    url: '/auth/swagger-auto-login',
    method: 'post'
  })
}
