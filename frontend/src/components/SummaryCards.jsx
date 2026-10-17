import { formatCurrency } from '../utils/format'

function SummaryCard({ icon, label, value, sublabel }) {
  return (
    <div className="summary-card">
      <div className="summary-card-icon" aria-hidden="true">
        {icon}
      </div>
      <div className="summary-card-body">
        <p className="summary-card-label">{label}</p>
        <p className="summary-card-value">{value}</p>
        {sublabel && <p className="summary-card-sublabel">{sublabel}</p>}
      </div>
    </div>
  )
}

const icons = {
  wallet: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 7H5a2 2 0 0 1 0-4h13v4" />
      <path d="M3 5v14a2 2 0 0 0 2 2h16v-9" />
      <path d="M18 12a2 2 0 0 0 0 4h4v-4Z" />
    </svg>
  ),
  count: (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
      <path d="M3 6h18" />
      <path d="M16 10a4 4 0 0 1-8 0" />
    </svg>
  )
}

export default function SummaryCards({ summary, expenseCount, selectedCategory, loading }) {
  const total = summary?.totalExpenses
  const scope = selectedCategory ? `In ${selectedCategory}` : 'Across all categories'

  return (
    <section className="summary-grid" aria-label="Expense summary">
      <SummaryCard
        icon={icons.wallet}
        label="Total Expenses"
        value={loading && total === undefined ? '…' : formatCurrency(total)}
        sublabel={scope}
      />
      <SummaryCard
        icon={icons.count}
        label="Number of Expenses"
        value={loading ? '…' : String(expenseCount)}
        sublabel={scope}
      />
    </section>
  )
}
