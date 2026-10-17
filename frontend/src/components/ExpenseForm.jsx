import { useEffect, useState } from 'react'
import { createExpense, getApiErrorMessage, getFieldErrors } from '../services/expenseApi'
import { todayISO } from '../utils/format'

const EMPTY_FORM = { title: '', amount: '', category: '', date: todayISO() }

function FieldError({ id, message }) {
  if (!message) {
    return null
  }
  return (
    <p className="field-error" id={id} role="alert">
      {message}
    </p>
  )
}

export default function ExpenseForm({ categories, onExpenseCreated }) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!success) {
      return undefined
    }
    const timer = setTimeout(() => setSuccess(''), 5000)
    return () => clearTimeout(timer)
  }, [success])

  function handleChange(event) {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFieldErrors((prev) => ({ ...prev, [name]: '' }))
    setFormError('')
  }

  function validate() {
    const errors = {}

    if (!form.title.trim()) {
      errors.title = 'Title is required'
    }

    if (!form.amount) {
      errors.amount = 'Amount is required'
    } else if (Number(form.amount) <= 0) {
      errors.amount = 'Amount must be greater than zero'
    }

    if (!form.category.trim()) {
      errors.category = 'Category is required'
    }

    if (!form.date) {
      errors.date = 'Date is required'
    }

    return errors
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSuccess('')
    setFormError('')

    const errors = validate()
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setSubmitting(true)
    try {
      await createExpense({
        title: form.title.trim(),
        amount: Number(form.amount),
        category: form.category.trim(),
        date: form.date
      })
      setForm(EMPTY_FORM)
      setFieldErrors({})
      setSuccess('Expense added successfully.')
      onExpenseCreated()
    } catch (error) {
      const serverErrors = getFieldErrors(error)
      if (serverErrors) {
        setFieldErrors(serverErrors)
      } else {
        setFormError(getApiErrorMessage(error, 'Failed to add expense.'))
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="form-card">
      <h2>Add Expense</h2>
      <p className="form-subtitle">Record a new expense below</p>

      {success && (
        <div className="banner banner-success" role="status">
          {success}
        </div>
      )}
      {formError && (
        <div className="banner banner-error" role="alert">
          {formError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div className="field">
          <label className="field-label" htmlFor="expense-title">
            Title
          </label>
          <input
            id="expense-title"
            name="title"
            type="text"
            placeholder="e.g. Grocery Shopping"
            autoComplete="off"
            value={form.title}
            onChange={handleChange}
            className={fieldErrors.title ? 'input-invalid' : ''}
            aria-invalid={fieldErrors.title ? true : undefined}
            aria-describedby={fieldErrors.title ? 'expense-title-error' : undefined}
          />
          <FieldError id="expense-title-error" message={fieldErrors.title} />
        </div>

        <div className="field">
          <label className="field-label" htmlFor="expense-amount">
            Amount
          </label>
          <input
            id="expense-amount"
            name="amount"
            type="number"
            min="0.01"
            step="0.01"
            inputMode="decimal"
            placeholder="0.00"
            value={form.amount}
            onChange={handleChange}
            className={fieldErrors.amount ? 'input-invalid' : ''}
            aria-invalid={fieldErrors.amount ? true : undefined}
            aria-describedby={fieldErrors.amount ? 'expense-amount-error' : undefined}
          />
          <FieldError id="expense-amount-error" message={fieldErrors.amount} />
        </div>

        <div className="field">
          <label className="field-label" htmlFor="expense-category">
            Category
          </label>
          <input
            id="expense-category"
            name="category"
            type="text"
            placeholder="e.g. Food, Transport"
            list="category-suggestions"
            autoComplete="off"
            value={form.category}
            onChange={handleChange}
            className={fieldErrors.category ? 'input-invalid' : ''}
            aria-invalid={fieldErrors.category ? true : undefined}
            aria-describedby={fieldErrors.category ? 'expense-category-error' : undefined}
          />
          <datalist id="category-suggestions">
            {categories.map((category) => (
              <option key={category} value={category} />
            ))}
          </datalist>
          <FieldError id="expense-category-error" message={fieldErrors.category} />
        </div>

        <div className="field">
          <label className="field-label" htmlFor="expense-date">
            Date
          </label>
          <input
            id="expense-date"
            name="date"
            type="date"
            value={form.date}
            onChange={handleChange}
            className={fieldErrors.date ? 'input-invalid' : ''}
            aria-invalid={fieldErrors.date ? true : undefined}
            aria-describedby={fieldErrors.date ? 'expense-date-error' : undefined}
          />
          <FieldError id="expense-date-error" message={fieldErrors.date} />
        </div>

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Adding…' : 'Add Expense'}
        </button>
      </form>
    </div>
  )
}
