import { useEffect, useState } from 'react'

import { createGroup } from '../services/groupApi'
import { getApiErrorMessage, getFieldErrors } from '../services/apiErrors'

const EMPTY_FORM = { name: '', members: '' }

export default function GroupForm({ onGroupCreated }) {
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

  async function handleSubmit(event) {
    event.preventDefault()
    setSuccess('')
    setFormError('')

    if (!form.name.trim()) {
      setFieldErrors({ name: 'Group name is required' })
      return
    }

    const memberUsernames = form.members
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)

    setSubmitting(true)
    try {
      await createGroup({ name: form.name.trim(), memberUsernames })
      setForm(EMPTY_FORM)
      setFieldErrors({})
      setSuccess('Group created successfully.')
      onGroupCreated()
    } catch (error) {
      const serverErrors = getFieldErrors(error)
      if (serverErrors) {
        setFieldErrors(serverErrors)
      } else {
        setFormError(getApiErrorMessage(error, 'Failed to create group.'))
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="form-card">
      <h2>Create Group</h2>
      <p className="form-subtitle">Split expenses with friends</p>

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
          <label className="field-label" htmlFor="group-name">
            Group name
          </label>
          <input
            id="group-name"
            name="name"
            type="text"
            placeholder="e.g. Trip to Goa"
            autoComplete="off"
            value={form.name}
            onChange={handleChange}
            className={fieldErrors.name ? 'input-invalid' : ''}
            aria-invalid={fieldErrors.name ? true : undefined}
          />
          {fieldErrors.name && (
            <p className="field-error" role="alert">
              {fieldErrors.name}
            </p>
          )}
        </div>

        <div className="field">
          <label className="field-label" htmlFor="group-members">
            Members (optional)
          </label>
          <input
            id="group-members"
            name="members"
            type="text"
            placeholder="Usernames, comma separated"
            autoComplete="off"
            value={form.members}
            onChange={handleChange}
          />
          <p className="field-hint">
            They must already have an account. You're added automatically.
          </p>
        </div>

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Creating…' : 'Create Group'}
        </button>
      </form>
    </div>
  )
}
