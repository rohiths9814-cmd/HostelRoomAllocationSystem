/**
 * Top header bar.
 * Shows the current page title, a hamburger menu for mobile, and admin info.
 */
import { useAuth } from '../context/AuthContext';

export default function Header({ title, onMenuClick }) {
  const { admin } = useAuth();

  const initial = admin?.username ? admin.username.charAt(0).toUpperCase() : 'A';

  return (
    <header className="header">
      <div className="header-left">
        <button className="header-menu-btn" onClick={onMenuClick} aria-label="Menu">
          ☰
        </button>
        <h1 className="header-title">{title}</h1>
      </div>

      <div className="header-right">
        <div className="header-admin">
          <div className="header-admin-avatar">{initial}</div>
          <span className="header-admin-name">{admin?.username || 'Admin'}</span>
        </div>
      </div>
    </header>
  );
}
