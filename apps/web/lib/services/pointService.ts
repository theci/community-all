import { apiClient } from '../api';
import type { PointInfo, PointTransaction, PointRanking } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const pointService = {
  getMyPoints: async (currentUserId?: number): Promise<PointInfo> => {
    const params = currentUserId ? { currentUserId } : {};
    const response = await apiClient.get<ApiResponse<PointInfo>>('/points/me', { params });
    return response.data.data;
  },

  getMyTransactions: async (page = 0, size = 20): Promise<PointTransaction[]> => {
    const response = await apiClient.get<PointTransaction[]>('/points/me/transactions', {
      params: { page, size },
    });
    return response.data;
  },

  getRanking: async (limit = 100): Promise<PointRanking[]> => {
    const response = await apiClient.get(`/points/ranking?size=${limit}`);

    // API 응답이 { success, data } 형태인지 확인
    if (response.data.success && response.data.data) {
      return Array.isArray(response.data.data) ? response.data.data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },

  usePoints: async (amount: number, reason: string): Promise<void> => {
    await apiClient.post('/points/me/use', { amount, reason });
  },
};
