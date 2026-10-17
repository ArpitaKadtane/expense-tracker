import { useAuth } from '../context/AuthContext'

export default function Header() {
  const { username, logout } = useAuth()

  return (
    <header className="header">
      <div className="header-inner">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            ₹
          </span>
          <div className="brand-text">
            <h1>Smart Expense Tracker</h1>
            <p>Track your spending, stay in control</p>
          </div>
        </div>

        {username && (
          <div className="header-user">
            <span className="header-user-name">{username}</span>
            <button type="button" className="btn-logout" onClick={logout}>
              Sign out
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
