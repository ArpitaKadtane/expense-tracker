import http from './http'

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

export { getApiErrorMessage, getFieldErrors } from './apiErrors'
