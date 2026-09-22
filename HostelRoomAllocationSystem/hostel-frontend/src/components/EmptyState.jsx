/** Empty state placeholder shown when a list has no data. */
export default function EmptyState({ icon = '📋', title, text, description, action }) {
  const body = text || description;
  return (
    <div className="empty-state">
      <div className="empty-state-icon">{icon}</div>
      <div className="empty-state-title">{title}</div>
      {body && <div className="empty-state-text">{body}</div>}
      {action && action}
    </div>
  );
}

