import { formatCurrency } from '../utils/format'

function netClass(net) {
  const value = Number(net)
  if (value > 0.005) {
    return 'balance-net is-positive'
  }
  if (value < -0.005) {
    return 'balance-net is-negative'
  }
  return 'balance-net'
}

export default function GroupBalances({ balances }) {
  if (!balances || balances.length === 0) {
    return <p className="empty-subtitle">No members yet.</p>
  }

  return (
    <div className="balance-table-wrap">
      <table className="balance-table">
        <thead>
          <tr>
            <th>Member</th>
            <th className="align-right">Paid</th>
            <th className="align-right">Owed</th>
            <th className="align-right">Net</th>
          </tr>
        </thead>
        <tbody>
          {balances.map((balance) => (
            <tr key={balance.username}>
              <td>{balance.username}</td>
              <td className="align-right">{formatCurrency(balance.paid)}</td>
              <td className="align-right">{formatCurrency(balance.owed)}</td>
              <td className="align-right">
                <span className={netClass(balance.net)}>
                  {formatCurrency(balance.net)}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <p className="balance-legend">
        Positive net means the group owes that member.
      </p>
    </div>
  )
}
