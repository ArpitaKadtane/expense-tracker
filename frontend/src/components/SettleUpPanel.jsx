import { formatCurrency } from '../utils/format'

export default function SettleUpPanel({ settlements }) {
  if (!settlements || settlements.length === 0) {
    return (
      <div className="settle-empty">
        <span aria-hidden="true">✅</span>
        <p>Everyone is settled up.</p>
      </div>
    )
  }

  return (
    <ol className="settle-list">
      {settlements.map((settlement, index) => (
        <li
          key={`${settlement.from}-${settlement.to}-${index}`}
          className="settle-item"
        >
          <span className="settle-party">{settlement.from}</span>
          <span className="settle-arrow" aria-hidden="true">
            pays
          </span>
          <span className="settle-party">{settlement.to}</span>
          <span className="settle-amount">{formatCurrency(settlement.amount)}</span>
        </li>
      ))}
    </ol>
  )
}
