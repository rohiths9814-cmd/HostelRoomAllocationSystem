/**
 * Confirmation dialog shown inside a modal overlay.
 * Used for delete confirmations and other destructive actions.
 */
export default function ConfirmDialog({
  title = 'Are you sure?',
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  variant = 'danger',
  onConfirm,
  onCancel,
  loading = false,
}) {
  const icon = variant === 'danger' ? '⚠️' : 'ℹ️';

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" style={{ maxWidth: 420 }} onClick={e => e.stopPropagation()}>
        <div className="confirm-dialog">
          <div className="confirm-dialog-icon">{icon}</div>
          <h3>{title}</h3>
          {message && <p>{message}</p>}
          <div className="confirm-dialog-actions">
            <button className="btn btn-outline" onClick={onCancel} disabled={loading}>
              {cancelText}
            </button>
            <button
              className={`btn ${variant === 'danger' ? 'btn-danger' : 'btn-primary'}`}
              onClick={onConfirm}
              disabled={loading}
            >
              {loading ? 'Processing...' : confirmText}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
