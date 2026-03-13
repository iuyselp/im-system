import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const isLoggedIn = ref(!!token)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
    isLoggedIn.value = true
  }

  function clearToken() {
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    userInfo.value = null
    isLoggedIn.value = false
  }

  async function getUserInfo() {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        userInfo.value = res.data
        return res.data
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
      throw error
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    clearToken,
    getUserInfo,
    setUserInfo
  }
})
