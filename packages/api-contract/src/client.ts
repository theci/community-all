import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

/**
 * API 응답 래퍼 타입
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

/**
 * 인증된 axios 클라이언트
 * - HTTP-only 쿠키로 토큰 자동 전송 (withCredentials: true)
 * - 401 에러 시 자동 로그아웃 처리
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
  withCredentials: true, // 쿠키 자동 전송 활성화
});

// Request interceptor: 쿠키는 withCredentials로 자동 전송되므로 별도 처리 불필요
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 쿠키는 withCredentials: true로 자동 전송됨
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor: 401 에러 처리
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        // localStorage의 인증 상태 초기화
        localStorage.removeItem('auth-storage');

        // 쿠키는 백엔드에서 관리하므로 여기서는 리다이렉트만 수행
        const currentPath = window.location.pathname;
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
