import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // Token 过期或无效，跳转登录
      if (res.code === 401 || res.code === 1003 || res.code === 1004) {
        const userStore = useUserStore()
        userStore.clearToken()
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// 封装请求方法
export function get(url, params) {
  return request({ url, method: 'get', params })
}

export function post(url, data) {
  return request({ url, method: 'post', data })
}

export function put(url, data) {
  return request({ url, method: 'put', data })
}

export function del(url) {
  return request({ url, method: 'delete' })
}

export default request
