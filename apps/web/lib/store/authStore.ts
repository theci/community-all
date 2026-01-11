'use client';

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { authService } from '../services';
import type { User, LoginRequest, RegisterRequest } from '../types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  hasHydrated: boolean;

  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  setUser: (user: User) => void;
  clearError: () => void;
  setHasHydrated: (state: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      hasHydrated: false,

      login: async (credentials) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authService.login(credentials);

          // 디버깅: 로그인 응답 확인
          console.log('Login response:', response);

          // 토큰은 HTTP-only 쿠키로 자동 설정되므로 localStorage 사용 불필요

          // 로그인 후 쿠키 확인
          console.log('Cookies after login:', document.cookie);

          // 쿠키가 설정되었는지 확인 (약간의 지연 후)
          setTimeout(() => {
            const cookies = document.cookie;
            console.log('Cookies after 100ms:', cookies);

            // 인증 쿠키가 없으면 경고
            if (!cookies || (!cookies.includes('accessToken') && !cookies.includes('Authorization'))) {
              console.warn('⚠️ 경고: 로그인 후 인증 쿠키가 설정되지 않았습니다!');
              console.warn('백엔드가 Set-Cookie 헤더를 제대로 보내고 있는지 확인하세요.');
              console.warn('CORS 설정에서 credentials: true가 허용되어 있는지 확인하세요.');
            } else {
              console.log('✅ 인증 쿠키가 정상적으로 설정되었습니다.');
            }
          }, 100);

          set({
            user: response.user,
            isAuthenticated: true,
            isLoading: false,
          });

          // 디버깅: 저장된 상태 확인
          console.log('Auth state after login:', { user: response.user, isAuthenticated: true });
        } catch (error: any) {
          console.error('Login error:', error);
          set({
            error: error.response?.data?.message || '로그인에 실패했습니다.',
            isLoading: false,
          });
          throw error;
        }
      },

      register: async (data) => {
        set({ isLoading: true, error: null });
        try {
          await authService.register(data);
          set({ isLoading: false });
        } catch (error: any) {
          set({
            error: error.response?.data?.message || '회원가입에 실패했습니다.',
            isLoading: false,
          });
          throw error;
        }
      },

      logout: async () => {
        try {
          // 백엔드에 로그아웃 요청하여 쿠키 제거
          await authService.logout();
        } catch (error) {
          console.error('Logout error:', error);
          // 에러가 발생해도 클라이언트 상태는 초기화
        } finally {
          set({
            user: null,
            isAuthenticated: false,
          });
        }
      },

      setUser: (user) => set({ user }),

      clearError: () => set({ error: null }),

      setHasHydrated: (state) => set({ hasHydrated: state }),
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true);
      },
    }
  )
);
