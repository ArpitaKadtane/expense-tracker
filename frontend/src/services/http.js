import axios from 'axios'

export const TOKEN_KEY = 'expense-tracker.token'
export const USERNAME_KEY = 'expense-tracker.username'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const http = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const isAuthRequest = error?.config?.url?.includes('/auth/')

    if (status === 401 && !isAuthRequest) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USERNAME_KEY)
      window.dispatchEvent(new Event('auth:unauthorized'))
    }

    return Promise.reject(error)
  }
)

export default http
