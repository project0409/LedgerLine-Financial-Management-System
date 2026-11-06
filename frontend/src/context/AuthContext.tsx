import React, { createContext, useState, useEffect, useCallback } from 'react';
import { User, LoginPayload, RegisterPayload } from '../types';
import { authService } from '../services/authService';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  isAdmin: boolean;
  isAccountHolder: boolean;
  isAuditor: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const refreshUser = useCallback(async () => {
    try {
      const currentUser = await authService.getCurrentUser();
      setUser(currentUser);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  const login = async (payload: LoginPayload) => {
    const res = await authService.login(payload);
    setUser(res.data.user);
  };

  const register = async (payload: RegisterPayload) => {
    await authService.register(payload);
    // After registration, log in
    await login({ email: payload.email, password: payload.password });
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  const roles = user?.roles || [];
  const isAdmin = roles.includes('ROLE_ADMIN');
  const isAccountHolder = roles.includes('ROLE_ACCOUNT_HOLDER');
  const isAuditor = roles.includes('ROLE_AUDITOR');

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        refreshUser,
        isAdmin,
        isAccountHolder,
        isAuditor,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
