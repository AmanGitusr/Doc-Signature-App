import React from 'react';
import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="panel">
      <h1>Page not found</h1>
      <Link to="/">Back to dashboard</Link>
    </div>
  );
}
