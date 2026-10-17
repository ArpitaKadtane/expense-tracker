export default function Header() {
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
      </div>
    </header>
  )
}
