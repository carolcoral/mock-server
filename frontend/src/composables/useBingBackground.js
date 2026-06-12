import { ref } from 'vue'

const DEFAULT_BG = '/default-bg.jpg'
const BING_PROXY_PATH = '/bing-hp'
const FETCH_TIMEOUT = 2000

// 模块级缓存：整个 SPA 生命周期只请求一次
let cachedUrl = null
let pendingPromise = null

/**
 * 获取 Bing 每日背景图片 URL，自动缓存避免重复请求。
 * Login 和 Register 页面共享同一份缓存。
 */
export function useBingBackground() {
  const bgImage = ref(cachedUrl || DEFAULT_BG)

  const fetchBingBg = async () => {
    // 已有缓存，直接使用
    if (cachedUrl) {
      bgImage.value = cachedUrl
      return
    }

    // 已有进行中的请求，等待其结果
    if (pendingPromise) {
      try {
        cachedUrl = await pendingPromise
        bgImage.value = cachedUrl
      } catch {
        bgImage.value = DEFAULT_BG
      }
      return
    }

    // 发起新请求
    pendingPromise = (async () => {
      const controller = new AbortController()
      const timeoutId = setTimeout(() => controller.abort(), FETCH_TIMEOUT)

      try {
        const response = await fetch(BING_PROXY_PATH, { signal: controller.signal })
        clearTimeout(timeoutId)
        if (!response.ok) throw new Error(`HTTP ${response.status}`)

        const data = await response.json()
        const images = data?.images || []
        if (images.length === 0) throw new Error('No images returned')

        const randomIndex = Math.floor(Math.random() * images.length)
        const image = images[randomIndex]
        const baseUrl = image.urlbase || image.url?.replace(/&pid=hp/, '')
        const fullUrl = baseUrl?.startsWith('http')
          ? baseUrl
          : `https://cn.bing.com${baseUrl}_1920x1080.jpg`

        const img = new Image()
        img.src = fullUrl
        await new Promise((resolve, reject) => {
          img.onload = () => resolve()
          img.onerror = () => reject(new Error('Image load failed'))
          setTimeout(() => reject(new Error('Image load timeout')), 2000)
        })

        return fullUrl
      } catch (error) {
        console.warn('Bing 每日图片获取失败，使用默认背景:', error.message || error)
        return DEFAULT_BG
      }
    })()

    try {
      cachedUrl = await pendingPromise
      bgImage.value = cachedUrl
    } catch {
      bgImage.value = DEFAULT_BG
    } finally {
      pendingPromise = null
    }
  }

  return { bgImage, fetchBingBg }
}
