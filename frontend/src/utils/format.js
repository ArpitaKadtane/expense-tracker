const currencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const dateFormatter = new Intl.DateTimeFormat('en-IN', {
  day: 'numeric',
  month: 'short',
  year: 'numeric'
})

export function formatCurrency(value) {
  const number = Number(value)
  if (Number.isNaN(number)) {
    return '—'
  }
  return currencyFormatter.format(number)
}

export function formatDate(dateString) {
  if (!dateString) {
    return '—'
  }
  const date = new Date(`${dateString}T00:00:00`)
  if (Number.isNaN(date.getTime())) {
    return dateString
  }
  return dateFormatter.format(date)
}

export function todayISO() {
  const now = new Date()
  const localTime = now.getTime() - now.getTimezoneOffset() * 60000
  return new Date(localTime).toISOString().slice(0, 10)
}

const CATEGORY_COLORS = [
  { bg: '#eef2ff', text: '#4338ca' },
  { bg: '#ecfdf5', text: '#047857' },
  { bg: '#fff7ed', text: '#c2410c' },
  { bg: '#fdf2f8', text: '#be185d' },
  { bg: '#eff6ff', text: '#1d4ed8' },
  { bg: '#fefce8', text: '#a16207' },
  { bg: '#f5f3ff', text: '#6d28d9' },
  { bg: '#ecfeff', text: '#0e7490' }
]

export function getCategoryColor(category) {
  if (!category) {
    return CATEGORY_COLORS[0]
  }
  let hash = 0
  for (let i = 0; i < category.length; i += 1) {
    hash = (hash * 31 + category.charCodeAt(i)) >>> 0
  }
  return CATEGORY_COLORS[hash % CATEGORY_COLORS.length]
}
