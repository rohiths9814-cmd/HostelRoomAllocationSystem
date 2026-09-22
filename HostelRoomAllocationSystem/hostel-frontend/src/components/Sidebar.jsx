/**
 * Sidebar navigation.
 *
 * Uses NavLink from react-router-dom so the active page is highlighted
 * automatically. Emoji icons are used instead of an icon library to
 * keep dependencies minimal.
 */
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
  { section: 'Overview' },
  { path: '/',              icon: '📊', label: 'Dashboard' },

  { section: 'Management' },
  { path: '/students',      icon: '🎓', label: 'Students' },
  { path: '/rooms',         icon: '🏠', label: 'Rooms' },
  { path: '/allocations',   icon: '🔑', label: 'Allocations' },
  { path: '/waiting-list',  icon: '⏳', label: 'Waiting List' },

  { section: 'Operations' },
  { path: '/complaints',    icon: '📝', label: 'Complaints' },
  { path: '/fees',          icon: '💰', label: 'Fees' },
  { path: '/reports',       icon: '📈', label: 'Reports' },
];

export default function Sidebar({ isOpen, onClose }) {
  const { logout } = useAuth();

  return (
    <>
      {/* Mobile overlay */}
      <div
        className={`mobile-overlay ${isOpen ? 'visible' : ''}`}
        onClick={onClose}
      />

      <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
        {/* Brand */}
        <div className="sidebar-brand">
          <div className="sidebar-brand-icon">H</div>
          <div className="sidebar-brand-text">
            <span className="sidebar-brand-name">HostelMS</span>
            <span className="sidebar-brand-label">Management System</span>
          </div>
        </div>

        {/* Navigation */}
        <nav className="sidebar-nav">
          {navItems.map((item, index) => {
            if (item.section) {
              return (
                <div key={index} className="sidebar-section-label">
                  {item.section}
                </div>
              );
            }
            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === '/'}
                className={({ isActive }) =>
                  `sidebar-link ${isActive ? 'active' : ''}`
                }
                onClick={onClose}
              >
                <span className="sidebar-link-icon">{item.icon}</span>
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Footer with logout */}
        <div className="sidebar-footer">
          <button className="sidebar-logout" onClick={logout}>
            <span className="sidebar-link-icon">🚪</span>
            <span>Logout</span>
          </button>
        </div>
      </aside>
    </>
  );
}
