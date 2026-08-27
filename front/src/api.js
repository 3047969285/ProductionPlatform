import axios from 'axios'
import { clearAuth, getToken } from './auth'
import { apiResultCode } from './constants'

const apiBase = import.meta.env.VITE_API_BASE_URL || '/api'

const http = axios.create({ baseURL: apiBase })

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body.code === apiResultCode.unauthorized) {
      clearAuth()
      window.location.href = '/login'
      return Promise.reject(new Error(body.message))
    }
    if (body.code !== apiResultCode.success) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (err) => {
    if (err.response?.status === apiResultCode.unauthorized) {
      clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(err.response?.data?.message ? new Error(err.response.data.message) : err)
  },
)

export default http
