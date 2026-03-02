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
