/**
 * Login Page
 * ----------
 * A split-screen login: visual on the left, form on the right.
 *
 * On submit, this calls POST /api/auth/login on the Java backend.
 * If the backend returns success, the admin is stored in AuthContext
 * and the user is redirected to the dashboard.
 *
 * If the backend rejects the credentials, the backend's error message
 * is displayed. We do NOT hardcode login checks in React.
 */
import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login as loginApi } from '../services/api';

export default function Login() {
  const { isAuthenticated, loginAdmin } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // If already logged in, go to dashboard
  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    // Basic frontend checks
    if (!username.trim()) {
      setError('Please enter your username.');
      return;
    }
    if (!password.trim()) {
      setError('Please enter your password.');
      return;
    }

    setLoading(true);
    try {
      // ─── This calls the Java backend ───
      const response = await loginApi(username, password);

      if (response.success) {
        // Store admin info in context & redirect
        loginAdmin(response.admin);
      } else {
        setError(response.message || 'Login failed.');
      }
    } catch (err) {
      // Network error or backend error message
      setError(err.message || 'An unexpected error occurred.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      {/* Left side — visual */}
      <div className="login-visual">
        <div className="login-visual-content">
          <div className="login-visual-icon">🏛️</div>
          <h2>Hostel Management System</h2>
          <p>
            A centralized platform for managing hostel room allocations,
            student records, complaints, and fee tracking.
          </p>
        </div>
      </div>

      {/* Right side — form */}
      <div className="login-form-side">
        <div className="login-form-container">
          <div className="login-form-header">
            <h1>Welcome back</h1>
            <p>Sign in to your admin account</p>
          </div>

          {error && <div className="login-error">{error}</div>}

          <form className="login-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username</label>
              <input
                id="username"
                className="form-input"
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="Enter your username"
                autoComplete="username"
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="password">Password</label>
              <div className="login-password-wrapper">
                <input
                  id="password"
                  className="form-input"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  disabled={loading}
                />
                <button
                  type="button"
                  className="login-password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? '🙈' : '👁️'}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary login-btn"
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
