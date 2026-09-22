/** Reusable modal component. */
export default function Modal({ title, onClose, children, wide = false }) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal"
        style={wide ? { maxWidth: 680 } : {}}
        onClick={e => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2 className="modal-title">{title}</h2>
          <button className="modal-close" onClick={onClose} aria-label="Close">×</button>
        </div>
        {children}
      </div>
    </div>
  );
}
