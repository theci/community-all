import { apiClient } from '../api';
import type { User, UserWithStats } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const userService = {
  // 사용자 정보 조회
  getUser: async (userId: number): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>(`/users/${userId}`);
    return response.data.data;
  },

  // 사용자 통계 포함 조회 (백엔드 API가 없으므로 기본 사용자 정보만 반환)
  getUserWithStats: async (userId: number): Promise<UserWithStats> => {
    const response = await apiClient.get<ApiResponse<User>>(
      `/users/${userId}`
    );

    // 기본 통계 값으로 설정 (실제 통계는 게시글/댓글 API로 계산 필요)
    const userWithStats: UserWithStats = {
      ...response.data.data,
      stats: {
        postCount: 0,
        commentCount: 0,
        likeCount: 0,
        scrapCount: 0,
      },
    };

    return userWithStats;
  },

  // 현재 사용자 정보 조회
  getMe: async (): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>('/users/me');
    return response.data.data;
  },

  // 프로필 업데이트
  updateProfile: async (data: {
    nickname?: string;
    bio?: string;
    avatarUrl?: string;
    website?: string;
    location?: string;
  }): Promise<User> => {
    const response = await apiClient.put<ApiResponse<User>>('/users/me/profile', data);
    return response.data.data;
  },
};
