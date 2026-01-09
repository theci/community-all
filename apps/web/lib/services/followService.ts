import { apiClient } from '@ddd3/api-contract';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const followService = {
  // 팔로우
  follow: async (targetUserId: number): Promise<void> => {
    await apiClient.post<ApiResponse<void>>(`/users/${targetUserId}/follow`);
  },

  // 언팔로우
  unfollow: async (targetUserId: number): Promise<void> => {
    await apiClient.delete(`/users/${targetUserId}/follow`);
  },

  // 팔로우 상태 확인
  getFollowStatus: async (targetUserId: number): Promise<{ isFollowing: boolean }> => {
    const response = await apiClient.get<ApiResponse<{ isFollowing: boolean }>>(
      `/users/${targetUserId}/follow/status`
    );
    return response.data.data;
  },

  // 팔로잉 목록 조회
  getFollowingList: async (userId: number): Promise<number[]> => {
    const response = await apiClient.get<ApiResponse<number[]>>(`/users/${userId}/following`);
    return response.data.data;
  },

  // 팔로워 목록 조회
  getFollowerList: async (userId: number): Promise<number[]> => {
    const response = await apiClient.get<ApiResponse<number[]>>(`/users/${userId}/followers`);
    return response.data.data;
  },

  // 팔로잉/팔로워 수 조회
  getFollowCount: async (userId: number): Promise<{ followingCount: number; followerCount: number }> => {
    const response = await apiClient.get<ApiResponse<{ followingCount: number; followerCount: number }>>(
      `/users/${userId}/follow/count`
    );
    return response.data.data;
  },
};
