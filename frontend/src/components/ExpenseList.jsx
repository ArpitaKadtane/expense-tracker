import { formatCurrency, formatDate, getCategoryColor } from '../utils/format'
import Pagination from './Pagination'

function TrashIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M3 6h18" />
      <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
      <path d="M10 11v6" />
      <path d="M14 11v6" />
    </svg>
  )
}

function ExpenseRow({ expense, onDeleteRequest }) {
  const colors = getCategoryColor(expense.category)

  return (
    <tr>
      <td data-label="Expense">
        <span className="expense-title">{expense.title}</span>
      </td>
      <td data-label="Category">
        <span
          className="category-badge"
          style={{ color: colors.text, backgroundColor: colors.bg }}
        >
          {expense.category}
        </span>
      </td>
      <td data-label="Date" className="expense-date">
        {formatDate(expense.date)}
      </td>
      <td data-label="Amount" className="expense-amount align-right">
        {formatCurrency(expense.amount)}
      </td>
      <td data-label="" className="action-cell">
        <button
          type="button"
          className="btn-icon btn-danger-icon"
          onClick={() => onDeleteRequest(expense)}
          aria-label={`Delete ${expense.title}`}
          title="Delete expense"
        >
          <TrashIcon />
        </button>
      </td>
    </tr>
  )
}

function EmptyState({ filtered }) {
  return (
    <div className="empty-state">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
        <path d="M3 6h18" />
        <path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
      <p className="empty-title">No expenses yet</p>
      <p className="empty-subtitle">
        {filtered
          ? 'There are no expenses in this category yet.'
          : 'Add your first expense to start tracking your spending.'}
      </p>
    </div>
  )
}

export default function ExpenseList({
  expenses,
  loading,
  refreshing,
  filtered,
  onDeleteRequest,
  page,
  totalPages,
  totalElements,
  onPageChange
}) {
  if (loading && expenses === null) {
    return (
      <div className="expense-list-loader">
        <span className="spinner" aria-hidden="true" />
        <p>Loading expenses…</p>
      </div>
    )
  }

  if (!expenses || expenses.length === 0) {
    return <EmptyState filtered={filtered} />
  }

  return (
    <>
      <div className={refreshing ? 'expense-list is-refreshing' : 'expense-list'}>
        <table className="expense-table">
          <thead>
            <tr>
              <th scope="col">Expense</th>
              <th scope="col">Category</th>
              <th scope="col">Date</th>
              <th scope="col" className="align-right">
                Amount
              </th>
              <th scope="col" className="align-right">
                <span className="sr-only">Action</span>
              </th>
            </tr>
          </thead>
          <tbody>
            {expenses.map((expense) => (
              <ExpenseRow
                key={expense.id}
                expense={expense}
                onDeleteRequest={onDeleteRequest}
              />
            ))}
          </tbody>
        </table>
      </div>

      <Pagination
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        onPageChange={onPageChange}
        disabled={refreshing}
      />
    </>
  )
}
