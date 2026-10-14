import axios from 'axios'
import { ref } from 'vue'
import { clearAuth, getToken } from './auth'

const apiBase = import.meta.env.VITE_API_BASE_URL || '/api'
const loginPath = `${import.meta.env.BASE_URL}login`

const http = axios.create({ baseURL: apiBase, timeout: 20000 })

// 连接状态：供导航栏状态灯使用。'idle' 未请求 / 'ok' 正常 / 'down' 不可达。
export const connection = ref({ state: 'idle', message: '', at: 0 })

let probeTimer
function markUp() {
  connection.value = { state: 'ok', message: '', at: Date.now() }
}
function markDown(message) {
  connection.value = { state: 'down', message: message || '服务暂时不可达', at: Date.now() }
}

// 后端冷启动（免费实例休眠唤醒）会偶发超时，避免状态灯反复闪烁。
function markDownDebounced(message) {
  clearTimeout(probeTimer)
  probeTimer = setTimeout(() => markDown(message), 1200)
}

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    clearTimeout(probeTimer)
    markUp()
    if (body.code === 401) {
      clearAuth()
      window.location.href = loginPath
      return Promise.reject(new Error(body.message))
    }
    if (body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (err) => {
    if (err.response?.status === 401) {
      clearAuth()
      window.location.href = loginPath
      return Promise.reject(err.response?.data?.message ? new Error(err.response.data.message) : err)
    }
    // 没有响应体说明请求没打到服务上（网络中断 / 跨域 / 超时）。
    if (!err.response) markDownDebounced(err.message)
    else markUp()
    return Promise.reject(err.response?.data?.message ? new Error(err.response.data.message) : err)
  },
)

export default http
