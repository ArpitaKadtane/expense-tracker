import { useCallback, useEffect, useState } from 'react'

import Banner from './Banner'
import AddGroupExpenseForm from './AddGroupExpenseForm'
import GroupBalances from './GroupBalances'
import SettleUpPanel from './SettleUpPanel'
import { formatCurrency, formatDate } from '../utils/format'
import {
  addGroupMember,
  getGroup,
  getGroupBalances,
  getGroupExpenses,
  getGroupSettleUp
} from '../services/groupApi'
import { getApiErrorMessage } from '../services/apiErrors'

const ChevronLeft = () => (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path
      d="M10 12L6 8l4-4"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="none"
    />
  </svg>
)

export default function GroupDetail({ groupId, onBack }) {
  const [group, setGroup] = useState(null)
  const [balances, setBalances] = useState([])
  const [settlements, setSettlements] = useState([])
  const [expenses, setExpenses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [memberInput, setMemberInput] = useState('')
  const [addingMember, setAddingMember] = useState(false)
  const [addError, setAddError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [g, b, s, e] = await Promise.all([
        getGroup(groupId),
        getGroupBalances(groupId),
        getGroupSettleUp(groupId),
        getGroupExpenses(groupId)
      ])
      setGroup(g)
      setBalances(b)
      setSettlements(s)
      setExpenses(e)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Failed to load group.'))
    } finally {
      setLoading(false)
    }
  }, [groupId])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    if (!notice) {
      return undefined
    }
    const timer = setTimeout(() => setNotice(''), 4000)
    return () => clearTimeout(timer)
  }, [notice])

  async function handleAddMember(event) {
    event.preventDefault()
    if (!memberInput.trim()) {
      return
    }
    setAddError('')
    setAddingMember(true)
    try {
      await addGroupMember(groupId, memberInput.trim())
      setMemberInput('')
      await load()
      setNotice('Member added.')
    } catch (requestError) {
      setAddError(getApiErrorMessage(requestError, 'Failed to add member.'))
    } finally {
      setAddingMember(false)
    }
  }

  if (loading) {
    return (
      <main className="container">
        <div className="expense-list-loader">
          <span className="spinner" aria-hidden="true" />
          <span>Loading group…</span>
        </div>
      </main>
    )
  }

  if (error) {
    return (
      <main className="container">
        <button type="button" className="back-link" onClick={onBack}>
          <ChevronLeft />
          Back to groups
        </button>
        <Banner variant="error" onDismiss={() => setError('')}>
          {error}
        </Banner>
      </main>
    )
  }

  if (!group) {
    return (
      <main className="container">
        <button type="button" className="back-link" onClick={onBack}>
          <ChevronLeft />
          Back to groups
        </button>
        <p>Group not found.</p>
      </main>
    )
  }

  return (
    <main className="container">
      <button type="button" className="back-link" onClick={onBack}>
        <ChevronLeft />
        Back to groups
      </button>

      {notice && (
        <Banner variant="success" onDismiss={() => setNotice('')}>
          {notice}
        </Banner>
      )}
      {addError && (
        <Banner variant="error" onDismiss={() => setAddError('')}>
          {addError}
        </Banner>
      )}

      <div className="group-detail-head">
        <div>
          <h1 className="group-detail-title">{group.name}</h1>
          <p className="group-detail-subtitle">Owner: {group.ownerUsername}</p>
        </div>
        <form className="add-member-form" onSubmit={handleAddMember}>
          <input
            type="text"
            placeholder="Username"
            value={memberInput}
            onChange={(e) => setMemberInput(e.target.value)}
            autoComplete="off"
          />
          <button
            type="submit"
            className="btn btn-secondary"
            disabled={addingMember}
          >
            {addingMember ? 'Adding…' : 'Add member'}
          </button>
        </form>
      </div>

      <div className="member-chips">
        {group.members?.map((m) => (
          <span key={m.username} className="chip">
            {m.username}
          </span>
        ))}
      </div>

      <div className="dashboard-grid group-detail-grid">
        <section className="panel" aria-label="Group expenses">
          <div className="panel-head">
            <h2>Expenses</h2>
          </div>
          {expenses.length === 0 ? (
            <div className="empty-state">
              <p className="empty-title">No expenses yet</p>
              <p className="empty-subtitle">Add the first group expense below.</p>
            </div>
          ) : (
            <div className="expense-table-wrap">
              <table className="expense-table group-expense-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Paid by</th>
                    <th>Date</th>
                    <th className="align-right">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {expenses.map((exp) => (
                    <tr key={exp.id}>
                      <td>
                        <div className="expense-title">{exp.title}</div>
                        <div className="expense-subline">{exp.category}</div>
                      </td>
                      <td>{exp.paidBy}</td>
                      <td className="expense-date">{formatDate(exp.date)}</td>
                      <td className="align-right">
                        <span className="expense-amount">
                          {formatCurrency(exp.amount)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <aside className="group-side" aria-label="Group balances and settle-up">
          <div className="panel">
            <div className="panel-head">
              <h2>Balances</h2>
            </div>
            <GroupBalances balances={balances} />
          </div>

          <div className="panel">
            <div className="panel-head">
              <h2>Settle Up</h2>
            </div>
            <SettleUpPanel settlements={settlements} />
          </div>

          <AddGroupExpenseForm group={group} onCreated={load} />
        </aside>
      </div>
    </main>
  )
}
