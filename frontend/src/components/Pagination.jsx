function ChevronIcon({ direction }) {
  const path = direction === 'left' ? 'M15 18l-6-6 6-6' : 'M9 6l6 6-6 6'

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
      <path d={path} />
    </svg>
  )
}

export default function Pagination({ page, totalPages, totalElements, onPageChange, disabled }) {
  if (totalPages <= 1) {
    return null
  }

  const isFirst = page <= 0
  const isLast = page >= totalPages - 1

  return (
    <nav className="pagination" aria-label="Expense pages">
      <button
        type="button"
        className="btn btn-secondary pagination-btn"
        onClick={() => onPageChange(page - 1)}
        disabled={disabled || isFirst}
      >
        <ChevronIcon direction="left" />
        Previous
      </button>

      <span className="pagination-status">
        Page {page + 1} of {totalPages}
        <span className="pagination-total"> · {totalElements} total</span>
      </span>

      <button
        type="button"
        className="btn btn-secondary pagination-btn"
        onClick={() => onPageChange(page + 1)}
        disabled={disabled || isLast}
      >
        Next
        <ChevronIcon direction="right" />
      </button>
    </nav>
  )
}
