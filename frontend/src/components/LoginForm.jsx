import { useState } from 'react'

import { useAuth } from '../context/AuthContext'

export default function LoginForm() {
  const { login, register } = useAuth()

  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const isRegister = mode === 'register'

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')

    if (username.trim().length < 3) {
      setError('Username must be at least 3 characters.')
      return
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.')
      return
    }

    setSubmitting(true)
    try {
      if (isRegister) {
        await register(username.trim(), password)
      } else {
        await login(username.trim(), password)
      }
    } catch (requestError) {
      const status = requestError?.response?.status
      if (status === 401) {
        setError('Invalid username or password.')
      } else if (status === 409) {
        setError('That username is already taken.')
      } else if (requestError?.response?.data?.message) {
        setError(requestError.response.data.message)
      } else if (requestError?.request) {
        setError('Could not reach the backend server.')
      } else {
        setError('Something went wrong. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  function switchMode() {
    setMode(isRegister ? 'login' : 'register')
    setError('')
  }

  return (
    <div className="auth-screen">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="brand-mark" aria-hidden="true">
            ₹
          </span>
          <h1>Smart Expense Tracker</h1>
          <p>{isRegister ? 'Create your account' : 'Welcome back'}</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <label className="field">
            <span className="field-label">Username</span>
            <input
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              placeholder="e.g. arpita"
              required
            />
          </label>

          <label className="field">
            <span className="field-label">Password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete={isRegister ? 'new-password' : 'current-password'}
              placeholder="At least 6 characters"
              required
            />
          </label>

          {error && (
            <p className="auth-error" role="alert">
              {error}
            </p>
          )}

          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting
              ? 'Please wait…'
              : isRegister
                ? 'Create account'
                : 'Sign in'}
          </button>
        </form>

        <p className="auth-switch">
          {isRegister ? 'Already have an account?' : 'New here?'}{' '}
          <button type="button" className="link-button" onClick={switchMode}>
            {isRegister ? 'Sign in' : 'Create an account'}
          </button>
        </p>
      </div>
    </div>
  )
}
