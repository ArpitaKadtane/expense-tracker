import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'

import * as authApi from '../services/authApi'
import { TOKEN_KEY, USERNAME_KEY } from '../services/http'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [username, setUsername] = useState(
    () => localStorage.getItem(USERNAME_KEY)
  )

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USERNAME_KEY)
    setToken(null)
    setUsername(null)
  }, [])

  useEffect(() => {
    function handleUnauthorized() {
      clearSession()
    }
    window.addEventListener('auth:unauthorized', handleUnauthorized)
    return () =>
      window.removeEventListener('auth:unauthorized', handleUnauthorized)
  }, [clearSession])

  const startSession = useCallback((data) => {
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USERNAME_KEY, data.username)
    setToken(data.token)
    setUsername(data.username)
  }, [])

  const login = useCallback(
    async (name, password) => {
      const data = await authApi.login(name, password)
      startSession(data)
    },
    [startSession]
  )

  const register = useCallback(
    async (name, password) => {
      const data = await authApi.register(name, password)
      startSession(data)
    },
    [startSession]
  )

  const value = useMemo(
    () => ({
      token,
      username,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout: clearSession
    }),
    [token, username, login, register, clearSession]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
