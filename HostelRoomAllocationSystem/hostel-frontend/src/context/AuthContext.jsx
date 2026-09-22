/**
 * AuthContext
 * -----------
 * Manages the logged-in admin state across the whole app.
 *
 * Uses React Context so any component can call useAuth() to get:
 * - admin: the current admin object (or null)
 * - isAuthenticated: boolean
 * - loginAdmin(admin): saves admin and redirects to dashboard
 * - logout(): clears session and redirects to login
 *
 * The admin data is stored in sessionStorage so it survives page refreshes
 * but is cleared when the browser tab is closed.
 */
import { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const navigate = useNavigate();

  // Try to restore from sessionStorage on mount
  const [admin, setAdmin] = useState(() => {
    const saved = sessionStorage.getItem('hostel_admin');
    return saved ? JSON.parse(saved) : null;
  });

  const isAuthenticated = admin !== null;

  function loginAdmin(adminData) {
    setAdmin(adminData);
    sessionStorage.setItem('hostel_admin', JSON.stringify(adminData));
    navigate('/');
  }

  function logout() {
    setAdmin(null);
    sessionStorage.removeItem('hostel_admin');
    navigate('/login');
  }

  return (
    <AuthContext.Provider value={{ admin, isAuthenticated, loginAdmin, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider');
  }
  return context;
}
