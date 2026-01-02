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
  UserManagement,
  UserDetail,
  UpdateUserStatusRequest,
  UpdateUserRoleRequest,
  AdjustUserPointRequest,
  UserStatus,
  UserRole,
  PostManagement,
  UpdatePostStatusRequest,
  MarkAsNoticeRequest,
  BulkPostActionRequest,
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

  /**
   * 사용자 목록 조회
   */
  async getUserList(params: {
    keyword?: string;
    status?: UserStatus;
    role?: UserRole;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: 'ASC' | 'DESC';
  }): Promise<{
    content: UserManagement[];
    pageInfo: {
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
    };
  }> {
    const response = await apiClient.get<ApiResponse<{
      content: UserManagement[];
      totalElements: number;
      totalPages: number;
      size: number;
      number: number;
    }>>('/admin/users', {
      params: {
        keyword: params.keyword,
        status: params.status,
        role: params.role,
        page: params.page || 0,
        size: params.size || 20,
        sortBy: params.sortBy || 'createdAt',
        sortDirection: params.sortDirection || 'DESC',
      },
    });

    return {
      content: response.data.data.content,
      pageInfo: {
        page: response.data.data.number,
        size: response.data.data.size,
        totalElements: response.data.data.totalElements,
        totalPages: response.data.data.totalPages,
      },
    };
  }

  /**
   * 사용자 상세 조회
   */
  async getUserDetail(userId: number): Promise<UserDetail> {
    const response = await apiClient.get<ApiResponse<UserDetail>>(`/admin/users/${userId}`);
    return response.data.data;
  }

  /**
   * 사용자 상태 변경
   */
  async updateUserStatus(userId: number, data: UpdateUserStatusRequest): Promise<void> {
    await apiClient.put(`/admin/users/${userId}/status`, data);
  }

  /**
   * 사용자 역할 변경
   */
  async updateUserRole(userId: number, data: UpdateUserRoleRequest): Promise<void> {
    await apiClient.put(`/admin/users/${userId}/role`, data);
  }

  /**
   * 사용자 포인트 조정
   */
  async adjustUserPoint(userId: number, data: AdjustUserPointRequest): Promise<void> {
    await apiClient.post(`/admin/users/${userId}/points`, data);
  }

  // ========== 콘텐츠 관리 (Content Moderation) ==========

  /**
   * 게시글 목록 조회 (관리자용)
   */
  async getPostList(params: {
    status?: string;
    keyword?: string;
    isNotice?: boolean;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: 'ASC' | 'DESC';
  }): Promise<{
    content: PostManagement[];
    pageInfo: {
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
    };
  }> {
    const response = await apiClient.get<ApiResponse<{
      content: PostManagement[];
      totalElements: number;
      totalPages: number;
      size: number;
      number: number;
    }>>('/admin/posts', {
      params: {
        status: params.status,
        keyword: params.keyword,
        isNotice: params.isNotice,
        page: params.page || 0,
        size: params.size || 20,
        sortBy: params.sortBy || 'createdAt',
        sortDirection: params.sortDirection || 'DESC',
      },
    });

    return {
      content: response.data.data.content,
      pageInfo: {
        page: response.data.data.number,
        size: response.data.data.size,
        totalElements: response.data.data.totalElements,
        totalPages: response.data.data.totalPages,
      },
    };
  }

  /**
   * 게시글 상태 변경
   */
  async updatePostStatus(
    postId: number,
    adminId: number,
    data: UpdatePostStatusRequest
  ): Promise<void> {
    await apiClient.put(`/admin/posts/${postId}/status`, data, {
      params: { adminId },
    });
  }

  /**
   * 게시글 공지사항 지정/해제
   */
  async markPostAsNotice(postId: number, data: MarkAsNoticeRequest): Promise<void> {
    await apiClient.put(`/admin/posts/${postId}/notice`, data);
  }

  /**
   * 게시글 일괄 처리
   */
  async bulkPostAction(adminId: number, data: BulkPostActionRequest): Promise<void> {
    await apiClient.post('/admin/posts/bulk-action', data, {
      params: { adminId },
    });
  }
}

export const adminService = new AdminService();
