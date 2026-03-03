import request from '@/utils/request'

/**
 * 获取首页统计数据
 * @returns {Promise}
 */
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}
