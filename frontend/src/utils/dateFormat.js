/*
* Copyright (c) 2026, XINDU.SITE，Author: LXW
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

import request from './request'

// 默认日期格式
const DEFAULT_FORMAT = 'YYYY-MM-DD'

// 缓存从服务器加载的日期格式
let cachedDateFormat = null

/**
 * 从服务器加载日期格式配置
 */
export const loadDateFormat = async () => {
  try {
    const response = await request.get('/system-config')
    if (response.code === 200 && response.data && response.data.dateFormat) {
      cachedDateFormat = response.data.dateFormat
    }
  } catch (error) {
    console.error('加载日期格式失败:', error)
  }
}

/**
 * 获取当前日期格式
 */
export const getDateFormat = () => {
  return cachedDateFormat || DEFAULT_FORMAT
}

/**
 * 设置日期格式（保存到服务器后调用）
 */
export const setDateFormat = (format) => {
  cachedDateFormat = format
}

/**
 * 根据配置的日期格式格式化时间字符串
 * @param {string} timeStr - ISO 时间字符串
 * @returns {string} 格式化后的日期时间
 */
export const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  if (isNaN(date.getTime())) return timeStr

  const format = getDateFormat()

  const pad = (n) => String(n).padStart(2, '0')
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hours = pad(date.getHours())
  const minutes = pad(date.getMinutes())
  const seconds = pad(date.getSeconds())

  let datePart
  switch (format) {
    case 'DD/MM/YYYY':
      datePart = `${day}/${month}/${year}`
      break
    case 'MM/DD/YYYY':
      datePart = `${month}/${day}/${year}`
      break
    case 'YYYY-MM-DD':
    default:
      datePart = `${year}-${month}-${day}`
      break
  }

  return `${datePart} ${hours}:${minutes}:${seconds}`
}
