import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const http = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' }
})

export async function getExpenses({ page = 0, size = 10, category } = {}) {
  const params = { page, size }
  if (category) {
    params.category = category
  }
  const { data } = await http.get('/expenses', { params })
  return data
}

export async function getCategories() {
  const { data } = await http.get('/expenses/categories')
  return data
}

export async function createExpense(expense) {
  const { data } = await http.post('/expenses', expense)
  return data
}

export async function updateExpense(id, expense) {
  const { data } = await http.put(`/expenses/${id}`, expense)
  return data
}

export async function deleteExpense(id) {
  await http.delete(`/expenses/${id}`)
}

export async function getSummary() {
  const { data } = await http.get('/expenses/summary')
  return data
}

export async function getSummaryByCategory(category) {
  const { data } = await http.get('/expenses/summary', {
    params: { category }
  })
  return data
}

export function getFieldErrors(error) {
  const errors = error?.response?.data?.errors
  return errors && typeof errors === 'object' ? errors : null
}

export function getApiErrorMessage(error, fallback) {
  if (error?.response) {
    const { status, data } = error.response

    if (status === 404) {
      return 'The requested expense could not be found. It may have already been deleted.'
    }

    if (data?.message) {
      return data.message
    }

    if (status >= 500) {
      return 'The server encountered an unexpected error. Please try again later.'
    }
  } else if (error?.request) {
    return 'Could not reach the backend server. Please make sure the API is running.'
  }

  return fallback
}
