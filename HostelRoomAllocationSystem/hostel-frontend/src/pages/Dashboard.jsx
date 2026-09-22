/**
 * Dashboard Page
 * --------------
 * The first page the admin sees after login.
 *
 * It fetches live statistics from GET /api/dashboard on the Java backend.
 * Nothing here is hardcoded — every number comes from MySQL through JDBC.
 *
 * The data flow is:
 *   React (useEffect → fetch)
 *     → Java DashboardHandler
 *       → multiple Service calls (StudentService, RoomService, etc.)
 *         → multiple DAO queries
 *           → MySQL
 *         → aggregated JSON response
 *     → React displays the numbers
 */
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getDashboardStats } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';

export default function Dashboard() {
  const { admin } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboard();
  }, []);

  async function fetchDashboard() {
    setLoading(true);
    setError('');
    try {
      const response = await getDashboardStats();
      if (response.success) {
        setStats(response.data);
      } else {
        setError(response.message || 'Failed to load dashboard');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // Determine greeting based on time of day
  function getGreeting() {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  }

  if (loading) {
    return <LoadingSpinner size="large" text="Loading dashboard..." />;
  }

  if (error) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '48px' }}>
        <p className="text-error" style={{ marginBottom: '16px' }}>{error}</p>
        <button className="btn btn-outline" onClick={fetchDashboard}>
          Try Again
        </button>
      </div>
    );
  }

  const occupancyPercent = stats?.occupancyPercent?.toFixed(1) || '0.0';

  return (
    <div>
      {/* Greeting */}
      <div className="dashboard-greeting">
        <h1>{getGreeting()}, {admin?.username || 'Admin'}</h1>
        <p>Here's today's hostel overview</p>
      </div>

      {/* Statistics cards */}
      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-card-icon blue">🎓</div>
          <div className="stat-card-label">Total Students</div>
          <div className="stat-card-value">{stats?.totalStudents ?? 0}</div>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon green">🏠</div>
          <div className="stat-card-label">Total Rooms</div>
          <div className="stat-card-value">{stats?.totalRooms ?? 0}</div>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon amber">📊</div>
          <div className="stat-card-label">Occupancy</div>
          <div className="stat-card-value">{occupancyPercent}%</div>
          <div className="stat-card-sub">
            {stats?.occupiedBeds ?? 0} of {stats?.totalBeds ?? 0} beds occupied
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon green">🛏️</div>
          <div className="stat-card-label">Available Beds</div>
          <div className="stat-card-value">{stats?.availableBeds ?? 0}</div>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon red">🚫</div>
          <div className="stat-card-label">Full Rooms</div>
          <div className="stat-card-value">{stats?.fullRooms ?? 0}</div>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon amber">📝</div>
          <div className="stat-card-label">Pending Complaints</div>
          <div className="stat-card-value">{stats?.pendingComplaints ?? 0}</div>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon red">💰</div>
          <div className="stat-card-label">Pending Fees</div>
          <div className="stat-card-value">{stats?.pendingFees ?? 0}</div>
        </div>

        <div className="stat-card">
          <div className="stat-card-icon blue">⏳</div>
          <div className="stat-card-label">Waiting Students</div>
          <div className="stat-card-value">{stats?.waitingStudents ?? 0}</div>
        </div>
      </div>
    </div>
  );
}
