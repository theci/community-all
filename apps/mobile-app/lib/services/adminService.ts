import { apiClient } from '../api';
import type {
  AdminStatistics,
  RoleStatistics,
  PointStatistics,
  Report,
  ReportDetail,
  ProcessReportRequest,
  Sanction,
  CreateSanctionRequest,
} from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

/**
 * Admin Service
 * 관리자 기능 관련 API 서비스
 */
class AdminService {
  /**
   * 관리자 통계 조회
   */
  async getStatistics(): Promise<AdminStatistics> {
    const response = await apiClient.get<ApiResponse<AdminStatistics>>('/admin/statistics');
    return response.data.data;
  }

  /**
   * 역할별 통계 조회
   */
  async getRoleStatistics(): Promise<RoleStatistics[]> {
    const response = await apiClient.get<ApiResponse<Record<string, number>>>('/roles/statistics');
    const statisticsMap = response.data.data;

    // Map 형태를 배열로 변환하고 percentage 계산
    const totalCount = Object.values(statisticsMap).reduce((sum, count) => sum + count, 0);

    return Object.entries(statisticsMap).map(([role, count]) => ({
      role,
      count,
      percentage: totalCount > 0 ? (count / totalCount) * 100 : 0
    }));
  }

  /**
   * 포인트 통계 조회
   */
  async getPointStatistics(): Promise<PointStatistics> {
    const response = await apiClient.get<ApiResponse<PointStatistics>>('/points/statistics/total');
    return response.data.data;
  }

  /**
   * 대기 중인 신고 목록 조회
   */
  async getPendingReports(page: number = 0, size: number = 20): Promise<{
    content: Report[];
    pageInfo: {
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
    };
  }> {
    const response = await apiClient.get<ApiResponse<{
      content: Report[];
      pageInfo: {
        page: number;
        size: number;
        totalElements: number;
        totalPages: number;
      };
    }>>('/reports/pending', {
      params: { page, size },
    });
    return response.data.data;
  }

  /**
   * 모든 신고 목록 조회
   */
  async getAllReports(
    page: number = 0,
    size: number = 20,
    status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  ): Promise<{
    content: Report[];
    pageInfo: {
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
    };
  }> {
    const response = await apiClient.get<ApiResponse<{
      content: Report[];
      pageInfo: {
        page: number;
        size: number;
        totalElements: number;
        totalPages: number;
      };
    }>>('/reports', {
      params: { page, size, status },
    });
    return response.data.data;
  }

  /**
   * 신고 상세 조회
   */
  async getReportDetail(reportId: number): Promise<ReportDetail> {
    const response = await apiClient.get<ApiResponse<ReportDetail>>(`/reports/${reportId}`);
    return response.data.data;
  }

  /**
   * 신고 처리 (승인/거부)
   */
  async processReport(
    reportId: number,
    userId: number,
    data: ProcessReportRequest
  ): Promise<Report> {
    const response = await apiClient.put<ApiResponse<Report>>(
      `/reports/${reportId}/process`,
      data,
      {
        params: { userId },
      }
    );
    return response.data.data;
  }

  /**
   * 사용자 제재 생성
   */
  async createSanction(
    userId: number,
    data: CreateSanctionRequest
  ): Promise<Sanction> {
    const response = await apiClient.post<ApiResponse<Sanction>>('/moderation/sanctions', data, {
      params: { userId },
    });
    return response.data.data;
  }

  /**
   * 사용자 제재 목록 조회
   */
  async getUserSanctions(targetUserId: number): Promise<Sanction[]> {
    const response = await apiClient.get<ApiResponse<Sanction[]>>('/moderation/sanctions', {
      params: { userId: targetUserId },
    });
    return response.data.data;
  }

  /**
   * 제재 취소
   */
  async cancelSanction(sanctionId: number, userId: number): Promise<void> {
    await apiClient.delete(`/moderation/sanctions/${sanctionId}`, {
      params: { userId },
    });
  }
}

export const adminService = new AdminService();
