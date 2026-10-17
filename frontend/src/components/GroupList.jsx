const UsersIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true">
    <path
      d="M16 19v-1.5a3.5 3.5 0 0 0-3.5-3.5h-5A3.5 3.5 0 0 0 4 17.5V19"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <circle cx="10" cy="8" r="3.2" />
    <path d="M20 19v-1.5a3.5 3.5 0 0 0-2.6-3.4" strokeLinecap="round" strokeLinejoin="round" />
    <path d="M15.4 5.1a3.2 3.2 0 0 1 0 5.8" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
)

export default function GroupList({ groups, loading, onSelect }) {
  if (loading) {
    return (
      <div className="expense-list-loader">
        <span className="spinner" aria-hidden="true" />
        <span>Loading groups…</span>
      </div>
    )
  }

  if (!groups || groups.length === 0) {
    return (
      <div className="empty-state">
        <UsersIcon />
        <p className="empty-title">No groups yet</p>
        <p className="empty-subtitle">
          Create a group to start splitting expenses with friends.
        </p>
      </div>
    )
  }

  return (
    <ul className="group-list">
      {groups.map((group) => (
        <li key={group.id}>
          <button
            type="button"
            className="group-card"
            onClick={() => onSelect(group.id)}
          >
            <span className="group-card-main">
              <span className="group-card-name">{group.name}</span>
              <span className="group-card-meta">
                {group.memberCount} member{group.memberCount === 1 ? '' : 's'} ·
                {' '}
                owned by {group.ownerUsername}
              </span>
            </span>
            <span className="group-card-chevron" aria-hidden="true">
              →
            </span>
          </button>
        </li>
      ))}
    </ul>
  )
}
