/**
 * DashboardLayout
 * ---------------
 * The main application shell: sidebar + header + content area.
 *
 * If the user is not authenticated, they are redirected to /login.
 * The Outlet renders whichever page matches the current route.
 */
import { useState } from 'react';
import { Outlet, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';

/** Map routes to human-readable page titles. */
const pageTitles = {
  '/': 'Dashboard',
  '/students': 'Students',
  '/rooms': 'Rooms',
  '/allocations': 'Allocations',
  '/waiting-list': 'Waiting List',
  '/complaints': 'Complaints',
  '/fees': 'Fees',
  '/reports': 'Reports',
};

export default function DashboardLayout() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Determine the page title from the current path
  const basePath = '/' + (location.pathname.split('/')[1] || '');
  const title = pageTitles[basePath] || 'Hostel Management';

  return (
    <div className="app-layout">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="main-area">
        <Header
          title={title}
          onMenuClick={() => setSidebarOpen(true)}
        />
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
