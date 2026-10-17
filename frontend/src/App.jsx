import { useCallback, useEffect, useRef, useState } from 'react'

import Header from './components/Header'
import SummaryCards from './components/SummaryCards'
import CategoryFilter from './components/CategoryFilter'
import ExpenseForm from './components/ExpenseForm'
import ExpenseList from './components/ExpenseList'
import ConfirmDialog from './components/ConfirmDialog'
import Banner from './components/Banner'
import LoginForm from './components/LoginForm'
import { AuthProvider, useAuth } from './context/AuthContext'

import {
  deleteExpense,
  getApiErrorMessage,
  getCategories,
  getExpenses,
  getSummary,
  getSummaryByCategory
} from './services/expenseApi'

const PAGE_SIZE = 10

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}

function AppRoutes() {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <Dashboard /> : <LoginForm />
}

function Dashboard() {
  const [expensePage, setExpensePage] = useState(null)
  const [categories, setCategories] = useState([])
  const [page, setPage] = useState(0)
  const [selectedCategory, setSelectedCategory] = useState('')
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [pendingDelete, setPendingDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)

  const hasLoaded = useRef(false)

  const refreshData = useCallback(async () => {
    if (hasLoaded.current) {
      setRefreshing(true)
    } else {
      setLoading(true)
    }
    setError('')

    try {
      const [pageData, categoryList, total] = await Promise.all([
        getExpenses({
          page,
          size: PAGE_SIZE,
          category: selectedCategory || undefined
        }),
        getCategories(),
        selectedCategory
          ? getSummaryByCategory(selectedCategory)
          : getSummary()
      ])

      if (
        pageData.content.length === 0 &&
        page > 0 &&
        pageData.totalElements > 0
      ) {
        setPage(Math.max(0, pageData.totalPages - 1))
        return
      }

      setExpensePage(pageData)
      setCategories(categoryList)
      setSummary(total)
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, 'Failed to load expenses.')
      )
    } finally {
      hasLoaded.current = true
      setLoading(false)
      setRefreshing(false)
    }
  }, [page, selectedCategory])

  useEffect(() => {
    refreshData()
  }, [refreshData])

  useEffect(() => {
    if (!notice) {
      return undefined
    }
    const timer = setTimeout(() => setNotice(''), 5000)
    return () => clearTimeout(timer)
  }, [notice])

  function handleCategoryChange(category) {
    setSelectedCategory(category)
    setPage(0)
  }

  async function handleDelete() {
    if (!pendingDelete) {
      return
    }

    setDeleting(true)
    try {
      await deleteExpense(pendingDelete.id)
      setPendingDelete(null)
      setNotice('Expense deleted successfully.')
      await refreshData()
    } catch (requestError) {
      setPendingDelete(null)
      setError(
        getApiErrorMessage(requestError, 'Failed to delete expense.')
      )
    } finally {
      setDeleting(false)
    }
  }

  const expenses = expensePage?.content ?? null

  return (
    <div className="app">
      <Header />

      <main className="container">
        {error && (
          <Banner variant="error" onDismiss={() => setError('')}>
            {error}
          </Banner>
        )}

        {notice && (
          <Banner variant="success" onDismiss={() => setNotice('')}>
            {notice}
          </Banner>
        )}

        <SummaryCards
          summary={summary}
          expenseCount={expensePage?.totalElements ?? 0}
          selectedCategory={selectedCategory}
          loading={loading}
        />

        <div className="dashboard-grid">
          <section className="panel" aria-label="Expenses">
            <div className="panel-head">
              <h2>Expenses</h2>
              <CategoryFilter
                categories={categories}
                value={selectedCategory}
                onChange={handleCategoryChange}
                disabled={loading && expensePage === null}
              />
            </div>

            <ExpenseList
              expenses={expenses}
              loading={loading}
              refreshing={refreshing}
              filtered={Boolean(selectedCategory)}
              onDeleteRequest={setPendingDelete}
              page={expensePage?.page ?? 0}
              totalPages={expensePage?.totalPages ?? 1}
              totalElements={expensePage?.totalElements ?? 0}
              onPageChange={setPage}
            />
          </section>

          <aside className="panel panel-form" aria-label="Add expense">
            <ExpenseForm categories={categories} onExpenseCreated={refreshData} />
          </aside>
        </div>
      </main>

      <footer className="footer">
        <p>
          Smart Expense Tracker · Java Spring Boot + React frontend
        </p>
      </footer>

      {pendingDelete && (
        <ConfirmDialog
          title="Delete expense?"
          message={`Are you sure you want to delete "${pendingDelete.title}"? This action cannot be undone.`}
          confirmLabel="Delete"
          busy={deleting}
          onConfirm={handleDelete}
          onCancel={() => setPendingDelete(null)}
        />
      )}
    </div>
  )
}
