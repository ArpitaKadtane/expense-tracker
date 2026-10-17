export default function Banner({ variant, children, onDismiss }) {
  return (
    <div className={`banner banner-${variant}`} role="alert">
      <span className="banner-text">{children}</span>
      {onDismiss && (
        <button
          type="button"
          className="banner-dismiss"
          onClick={onDismiss}
          aria-label="Dismiss message"
        >
          ×
        </button>
      )}
    </div>
  )
}
