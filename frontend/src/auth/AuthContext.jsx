import React, { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = api.getToken();
    if (!token) {
      setLoading(false);
      return;
    }
    api.me()
      .then(setUser)
      .catch(() => {
        api.setToken(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const value = {
    user,
    loading,
    async login(payload) {
      const response = await api.login(payload);
      api.setToken(response.token);
      setUser(response.user);
    },
    async register(payload) {
      const response = await api.register(payload);
      api.setToken(response.token);
      setUser(response.user);
    },
    logout() {
      api.setToken(null);
      setUser(null);
    }
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
