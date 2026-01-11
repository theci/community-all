import { apiClient } from '@ddd3/api-contract';
import type { User, UserWithStats } from '@ddd3/types';

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
    profileImageUrl?: string;
    bio?: string;
    birthDate?: string; // yyyy-MM-dd 형식
    gender?: 'MALE' | 'FEMALE' | 'OTHER';
  }): Promise<void> => {
    await apiClient.put<ApiResponse<void>>('/users/me/profile', data);
  },

  // 추천 사용자 목록 조회
  getSuggestedUsers: async (page = 0, size = 10): Promise<User[]> => {
    try {
      const response = await apiClient.get<ApiResponse<{ content: User[] }>>(
        `/users?page=${page}&size=${size}`
      );
      console.log('Raw API response:', response.data);
      return response.data.data.content || [];
    } catch (error) {
      console.error('Error fetching suggested users:', error);
      throw error;
    }
  },
};
