import { apiClient } from '@ddd3/api-contract';
import type { PointInfo, PointTransaction, PointRanking } from '@ddd3/types';

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

  getRanking: async (limit = 100): Promise<PointInfo[]> => {
    const response = await apiClient.get<ApiResponse<{ content: PointInfo[] }>>(`/points/ranking?size=${limit}`);
    return response.data.data?.content || [];
  },

  usePoints: async (amount: number, reason: string): Promise<void> => {
    await apiClient.post('/points/me/use', { amount, reason });
  },
};
