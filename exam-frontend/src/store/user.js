import { defineStore } from 'pinia'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    roles: (state) => state.userInfo?.roles || []
  },
  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.token = res.data.token
      setToken(res.data.token)
      return res.data
    },
    async fetchUserInfo() {
      const res = await getCurrentUser()
      this.userInfo = res.data
      return res.data
    },
    logout() {
      this.token = ''
      this.userInfo = null
      removeToken()
    }
  }
})
