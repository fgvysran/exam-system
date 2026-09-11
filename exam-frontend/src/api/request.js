import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器: 携带 token (后端 Sa-Token 的 header 名是 satoken)
request.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

// 响应拦截器: 统一处理 code
request.interceptors.response.use(
  response => {
    // 文件下载(blob)直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      removeToken()
      router.push('/login')
    } else {
      ElMessage.error(res.message || '请求失败')
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
