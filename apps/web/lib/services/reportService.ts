import { apiClient } from '@ddd3/api-contract';
import type { ReportCreateRequest } from '@ddd3/types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

interface ReportResponse {
  id: number;
  reporterId: number;
  reportedUserId: number;
  targetType: string;
  targetId: number;
  reason: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export const reportService = {
  /**
   * 신고 생성
   * @param data 신고 생성 요청 데이터
   * @param currentUserId 현재 사용자 ID
   * @returns 생성된 신고 정보
   */
  createReport: async (
    data: ReportCreateRequest,
    currentUserId: number
  ): Promise<ReportResponse> => {
    const response = await apiClient.post<ApiResponse<ReportResponse>>(
      `/reports?currentUserId=${currentUserId}`,
      data
    );
    return response.data.data;
  },
};
