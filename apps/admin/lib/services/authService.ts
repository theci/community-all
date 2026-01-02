import { apiClient } from '../api';
import type { LoginRequest, RegisterRequest, AuthResponse, RefreshTokenRequest } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
    console.log('Raw API response:', response.data);
    return response.data.data;
  },

  register: async (data: RegisterRequest): Promise<{ userId: number; email: string; username: string }> => {
    const response = await apiClient.post<ApiResponse<{ userId: number; email: string; username: string }>>('/users/register', data);
    return response.data.data;
  },

  refreshToken: async (data: RefreshTokenRequest): Promise<{ accessToken: string }> => {
    const response = await apiClient.post<ApiResponse<{ accessToken: string }>>('/auth/refresh', data);
    return response.data.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },
};
