import axios from 'axios'
import { clearAuth, getToken } from './auth'

const apiBase = import.meta.env.VITE_API_BASE_URL || '/api'
const loginPath = `${import.meta.env.BASE_URL}login`

const http = axios.create({ baseURL: apiBase })

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
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
    }
    return Promise.reject(err.response?.data?.message ? new Error(err.response.data.message) : err)
  },
)

export default http
