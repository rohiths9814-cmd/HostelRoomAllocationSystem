/**
 * App.jsx — Root component with React Router.
 *
 * Route structure:
 *   /login          → Login page (no sidebar/header)
 *   /               → Dashboard (inside DashboardLayout)
 *   /students       → Student management
 *   /rooms          → Room management
 *   /allocations    → Room allocation
 *   /waiting-list   → Waiting list
 *   /complaints     → Complaints
 *   /fees           → Fee management
 *   /reports        → Reports
 */
import { Routes, Route } from 'react-router-dom';

// Layouts
import DashboardLayout from './layouts/DashboardLayout';

// Pages
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Students from './pages/Students';
import Rooms from './pages/Rooms';
import Allocation from './pages/Allocation';
import WaitingList from './pages/WaitingList';
import Complaints from './pages/Complaints';
import Fees from './pages/Fees';
import Reports from './pages/Reports';

export default function App() {
  return (
    <Routes>
      {/* Login — standalone page, no sidebar */}
      <Route path="/login" element={<Login />} />

      {/* Dashboard layout — sidebar + header + content */}
      <Route path="/" element={<DashboardLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="students" element={<Students />} />
        <Route path="rooms" element={<Rooms />} />
        <Route path="allocations" element={<Allocation />} />
        <Route path="waiting-list" element={<WaitingList />} />
        <Route path="complaints" element={<Complaints />} />
        <Route path="fees" element={<Fees />} />
        <Route path="reports" element={<Reports />} />
      </Route>
    </Routes>
  );
}
