import { useCallback, useEffect, useState } from 'react'

import Banner from './Banner'
import GroupForm from './GroupForm'
import GroupList from './GroupList'
import GroupDetail from './GroupDetail'
import { getGroups } from '../services/groupApi'
import { getApiErrorMessage } from '../services/apiErrors'

export default function GroupsPage() {
  const [groups, setGroups] = useState(null)
  const [selectedId, setSelectedId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadGroups = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setGroups(await getGroups())
    } catch (requestError) {
      setGroups([])
      setError(getApiErrorMessage(requestError, 'Failed to load groups.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadGroups()
  }, [loadGroups])

  if (selectedId != null) {
    return <GroupDetail groupId={selectedId} onBack={() => setSelectedId(null)} />
  }

  return (
    <main className="container">
      {error && (
        <Banner variant="error" onDismiss={() => setError('')}>
          {error}
        </Banner>
      )}

      <div className="dashboard-grid">
        <section className="panel" aria-label="Your groups">
          <div className="panel-head">
            <h2>Your Groups</h2>
          </div>
          <GroupList groups={groups} loading={loading} onSelect={setSelectedId} />
        </section>

        <aside className="panel panel-form" aria-label="Create group">
          <GroupForm onGroupCreated={loadGroups} />
        </aside>
      </div>
    </main>
  )
}
