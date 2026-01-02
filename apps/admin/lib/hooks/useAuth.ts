'use client';

import { useAuthStore } from '../store';

export function useAuth() {
  const { user, isAuthenticated, isLoading, error, hasHydrated, login, register, logout, clearError } =
    useAuthStore();

  return {
    user,
    isAuthenticated,
    isLoading: isLoading || !hasHydrated, // hydration 완료 전까지 로딩 상태
    error,
    login,
    register,
    logout,
    clearError,
  };
}
