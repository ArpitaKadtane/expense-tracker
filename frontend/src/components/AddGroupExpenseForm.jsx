import { useEffect, useMemo, useState } from 'react'

import { addGroupExpense } from '../services/groupApi'
import { getApiErrorMessage, getFieldErrors } from '../services/apiErrors'
import { todayISO } from '../utils/format'
import { useAuth } from '../context/AuthContext'

const SPLIT_TYPES = [
  { value: 'EQUAL', label: 'Equal' },
  { value: 'EXACT', label: 'Exact amounts' },
  { value: 'PERCENTAGE', label: 'Percentages' }
]

function initialParticipants(members, currentUsername) {
  const base = (members || []).map((member) => ({
    username: member.username,
    selected: true,
    value: ''
  }))

  if (currentUsername && base.length > 0) {
    const exists = base.some((p) => p.username === currentUsername)
    if (!exists) {
      base.push({ username: currentUsername, selected: true, value: '' })
    }
  }

  return base
}

export default function AddGroupExpenseForm({ group, onCreated }) {
  const { username: currentUsername } = useAuth()
  const [form, setForm] = useState({
    title: '',
    amount: '',
    category: '',
    date: todayISO(),
    paidBy: currentUsername || '',
    splitType: 'EQUAL'
  })
  const [participants, setParticipants] = useState(() =>
    initialParticipants(group?.members, currentUsername)
  )
  const [fieldErrors, setFieldErrors] = useState({})
  const [formError, setFormError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      paidBy: prev.paidBy || currentUsername || ''
    }))
  }, [currentUsername])

  useEffect(() => {
    setParticipants(initialParticipants(group?.members, currentUsername))
  }, [group, currentUsername])

  useEffect(() => {
    if (!success) {
      return undefined
    }
    const timer = setTimeout(() => setSuccess(''), 4000)
    return () => clearTimeout(timer)
  }, [success])

  function handleChange(event) {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFieldErrors((prev) => ({ ...prev, [name]: '' }))
    setFormError('')
  }

  function toggleParticipant(username) {
    setParticipants((prev) =>
      prev.map((p) =>
        p.username === username ? { ...p, selected: !p.selected } : p
      )
    )
  }

  function updateParticipantValue(username, value) {
    setParticipants((prev) =>
      prev.map((p) => (p.username === username ? { ...p, value } : p))
    )
  }

  function setAll(selected) {
    setParticipants((prev) => prev.map((p) => ({ ...p, selected })))
  }

  const selectedParticipants = useMemo(
    () => participants.filter((p) => p.selected),
    [participants]
  )

  function validate() {
    const errors = {}
    if (!form.title.trim()) {
      errors.title = 'Title is required'
    }
    if (!form.amount) {
      errors.amount = 'Amount is required'
    } else {
      const num = Number(form.amount)
      if (Number.isNaN(num) || num <= 0) {
        errors.amount = 'Amount must be greater than zero'
      }
    }
    if (!form.category.trim()) {
      errors.category = 'Category is required'
    }
    if (!form.date) {
      errors.date = 'Date is required'
    }
    if (!form.paidBy.trim()) {
      errors.paidBy = 'Payer is required'
    }
    if (selectedParticipants.length === 0) {
      errors.participants = 'Select at least one participant'
    }
    if (form.splitType !== 'EQUAL' && selectedParticipants.length > 0) {
      const total = Number(form.amount) || 0
      let sum = 0
      for (const p of selectedParticipants) {
        if (!p.value) {
          errors.participants = 'All selected participants must have a value'
          break
        }
        const num = Number(p.value)
        if (Number.isNaN(num) || num <= 0) {
          errors.participants = 'Values must be greater than zero'
          break
        }
        sum += num
      }
      if (!errors.participants && form.splitType === 'EXACT') {
        if (Math.abs(sum - total) > 0.001) {
          errors.participants = `Exact amounts must sum to ${total.toFixed(2)}`
        }
      }
      if (!errors.participants && form.splitType === 'PERCENTAGE') {
        if (Math.abs(sum - 100) > 0.001) {
          errors.participants = 'Percentages must sum to 100'
        }
      }
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
      const payload = {
        title: form.title.trim(),
        amount: Number(form.amount),
        category: form.category.trim(),
        date: form.date,
        paidBy: form.paidBy.trim(),
        splitType: form.splitType
      }

      if (form.splitType === 'EQUAL') {
        payload.participants = selectedParticipants.map((p) => ({ username: p.username }))
      } else if (form.splitType === 'EXACT') {
        payload.participants = selectedParticipants.map((p) => ({
          username: p.username,
          value: Number(p.value)
        }))
      } else {
        payload.participants = selectedParticipants.map((p) => ({
          username: p.username,
          value: Number(p.value)
        }))
      }

      await addGroupExpense(group.id, payload)
      setSuccess('Group expense added.')
      onCreated()
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
      <h3>Add Group Expense</h3>

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

      <form onSubmit={handleSubmit} noValidate className="group-expense-form">
        <div className="field">
          <label className="field-label" htmlFor="ge-title">Title</label>
          <input
            id="ge-title"
            name="title"
            value={form.title}
            onChange={handleChange}
            placeholder="e.g. Dinner"
            className={fieldErrors.title ? 'input-invalid' : ''}
          />
          {fieldErrors.title && <p className="field-error">{fieldErrors.title}</p>}
        </div>

        <div className="field-row">
          <div className="field">
            <label className="field-label" htmlFor="ge-amount">Amount</label>
            <input
              id="ge-amount"
              name="amount"
              type="number"
              min="0.01"
              step="0.01"
              inputMode="decimal"
              value={form.amount}
              onChange={handleChange}
              className={fieldErrors.amount ? 'input-invalid' : ''}
            />
            {fieldErrors.amount && <p className="field-error">{fieldErrors.amount}</p>}
          </div>
          <div className="field">
            <label className="field-label" htmlFor="ge-date">Date</label>
            <input
              id="ge-date"
              name="date"
              type="date"
              value={form.date}
              onChange={handleChange}
              className={fieldErrors.date ? 'input-invalid' : ''}
            />
            {fieldErrors.date && <p className="field-error">{fieldErrors.date}</p>}
          </div>
        </div>

        <div className="field">
          <label className="field-label" htmlFor="ge-category">Category</label>
          <input
            id="ge-category"
            name="category"
            value={form.category}
            onChange={handleChange}
            placeholder="e.g. Food"
            className={fieldErrors.category ? 'input-invalid' : ''}
          />
          {fieldErrors.category && <p className="field-error">{fieldErrors.category}</p>}
        </div>

        <div className="field">
          <label className="field-label" htmlFor="ge-paidBy">Paid by</label>
          <select
            id="ge-paidBy"
            name="paidBy"
            value={form.paidBy}
            onChange={handleChange}
            className={fieldErrors.paidBy ? 'input-invalid' : ''}
          >
            {(group?.members || []).map((m) => (
              <option key={m.username} value={m.username}>
                {m.username}
              </option>
            ))}
          </select>
          {fieldErrors.paidBy && <p className="field-error">{fieldErrors.paidBy}</p>}
        </div>

        <div className="field">
          <label className="field-label" htmlFor="ge-split">Split type</label>
          <select
            id="ge-split"
            name="splitType"
            value={form.splitType}
            onChange={handleChange}
          >
            {SPLIT_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </div>

        <div className="participants-panel">
          <div className="participants-head">
            <span className="field-label">Participants</span>
            <div className="participants-actions">
              <button type="button" className="link-button" onClick={() => setAll(true)}>
                Select all
              </button>
              <button type="button" className="link-button" onClick={() => setAll(false)}>
                Clear
              </button>
            </div>
          </div>
          {participants.map((p) => (
            <div key={p.username} className="participant-row">
              <label className="participant-label">
                <input
                  type="checkbox"
                  checked={p.selected}
                  onChange={() => toggleParticipant(p.username)}
                />
                <span>{p.username}</span>
              </label>
              {form.splitType !== 'EQUAL' && (
                <input
                  type="number"
                  min="0.01"
                  step={form.splitType === 'PERCENTAGE' ? '1' : '0.01'}
                  value={p.value}
                  onChange={(e) => updateParticipantValue(p.username, e.target.value)}
                  placeholder={form.splitType === 'PERCENTAGE' ? '%' : 'Amount'}
                  className="participant-value"
                  disabled={!p.selected}
                />
              )}
            </div>
          ))}
          {fieldErrors.participants && (
            <p className="field-error">{fieldErrors.participants}</p>
          )}
        </div>

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Adding…' : 'Add Expense'}
        </button>
      </form>
    </div>
  )
}
