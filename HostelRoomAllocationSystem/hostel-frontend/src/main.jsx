/**
 * main.jsx — The entry point of the React application.
 *
 * Wraps the app with:
 *   1. BrowserRouter — for React Router
 *   2. AuthProvider  — for login state
 *   3. ToastProvider — for toast notifications
 */
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './components/Toast';
import App from './App';
import './index.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <App />
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>
);
