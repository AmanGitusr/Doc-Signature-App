import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" className="brand">Document Signature</Link>
        <div className="topbar-actions">
          {user ? <span className="muted">{user.fullName}</span> : null}
          {user ? <button className="ghost" onClick={handleLogout}>Logout</button> : null}
        </div>
      </header>
      <main className="app-content">{children}</main>
    </div>
  );
}
