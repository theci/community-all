import { apiClient } from '../api';
import type {
  Post,
  PostCreateRequest,
  PostUpdateRequest,
  PostListResponse,
  PostSearchRequest,
} from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const postService = {
  getPosts: async (page = 0, size = 20, sort = 'createdAt,desc'): Promise<PostListResponse> => {
    const response = await apiClient.get<ApiResponse<PostListResponse>>(
      `/posts?page=${page}&size=${size}&sort=${sort}`
    );
    return response.data.data;
  },

  getPost: async (postId: number): Promise<Post> => {
    const response = await apiClient.get<ApiResponse<Post>>(`/posts/${postId}`);
    return response.data.data;
  },

  searchPosts: async (params: PostSearchRequest, page = 0, size = 20): Promise<PostListResponse> => {
    const response = await apiClient.get<ApiResponse<PostListResponse>>('/posts/search', {
      params: { ...params, page, size },
    });
    return response.data.data;
  },

  getPopularPosts: async (limit = 10): Promise<Post[]> => {
    const response = await apiClient.get(`/posts/popular?limit=${limit}`);

    // 응답이 { success, data } 형태인지 확인
    if (response.data.success && response.data.data) {
      return Array.isArray(response.data.data) ? response.data.data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },

  getTrendingPosts: async (days = 7): Promise<Post[]> => {
    const response = await apiClient.get(`/posts/trending?days=${days}`);

    // 응답이 { success, data } 형태인지 확인
    if (response.data.success && response.data.data) {
      return Array.isArray(response.data.data) ? response.data.data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },

  createPost: async (data: PostCreateRequest, currentUserId: number): Promise<Post> => {
    const response = await apiClient.post<ApiResponse<Post>>(`/posts?currentUserId=${currentUserId}`, data);
    return response.data.data;
  },

  updatePost: async (postId: number, data: PostUpdateRequest, currentUserId: number): Promise<Post> => {
    const response = await apiClient.put<ApiResponse<Post>>(`/posts/${postId}?currentUserId=${currentUserId}`, data);
    return response.data.data;
  },

  deletePost: async (postId: number, currentUserId: number): Promise<void> => {
    await apiClient.delete(`/posts/${postId}?currentUserId=${currentUserId}`);
  },

  publishPost: async (postId: number, currentUserId: number): Promise<Post> => {
    const response = await apiClient.post<ApiResponse<Post>>(`/posts/${postId}/publish?currentUserId=${currentUserId}`);
    return response.data.data;
  },

  toggleLike: async (postId: number, currentUserId: number): Promise<{ liked: boolean; likeCount: number }> => {
    const response = await apiClient.post<ApiResponse<{ isLiked: boolean; totalLikeCount: number }>>(`/posts/${postId}/like?currentUserId=${currentUserId}`);
    // 백엔드 응답을 프론트엔드 형식으로 변환
    return {
      liked: response.data.data.isLiked,
      likeCount: response.data.data.totalLikeCount,
    };
  },

  getLikeStatus: async (postId: number, currentUserId?: number): Promise<{ liked: boolean }> => {
    const params = currentUserId ? { currentUserId } : {};
    const response = await apiClient.get<ApiResponse<{ liked: boolean }>>(`/posts/${postId}/like/status`, { params });
    return response.data.data;
  },

  toggleScrap: async (postId: number, currentUserId: number, currentScrapedState: boolean, folderId?: number): Promise<{ scraped: boolean }> => {
    // 현재 스크랩 상태를 기준으로 적절한 API 호출
    if (currentScrapedState) {
      // 이미 스크랩되어 있으면 DELETE로 취소
      await apiClient.delete(`/posts/${postId}/scrap?currentUserId=${currentUserId}`);
      return { scraped: false };
    } else {
      // 스크랩되어 있지 않으면 POST로 추가
      const requestBody = {
        postId,
        folderId: folderId || null,
      };
      await apiClient.post(`/posts/${postId}/scrap?currentUserId=${currentUserId}`, requestBody);
      return { scraped: true };
    }
  },

  getScrapStatus: async (postId: number, currentUserId?: number): Promise<{ scraped: boolean }> => {
    const params = currentUserId ? { currentUserId } : {};
    const response = await apiClient.get<ApiResponse<boolean>>(`/posts/${postId}/scrap/status`, { params });

    // 백엔드는 data: true/false (불리언)를 반환, 프론트엔드는 { scraped: boolean } 형태 필요
    const scrapedValue = response.data.data;
    return { scraped: scrapedValue };
  },

  // 특정 작성자의 게시글 조회
  getPostsByAuthor: async (authorId: number, page = 0, size = 20): Promise<Post[]> => {
    const response = await apiClient.get<ApiResponse<PostListResponse>>(
      `/posts/author/${authorId}?page=${page}&size=${size}`
    );
    return response.data.data.content;
  },

  // 좋아요한 게시글 조회
  getLikedPosts: async (userId: number, page = 0, size = 20): Promise<Post[]> => {
    const response = await apiClient.get(
      `/posts/likes/me?currentUserId=${userId}&page=${page}&size=${size}`
    );

    // 응답이 { success, data } 형태인지 확인
    if (response.data.success && response.data.data) {
      return Array.isArray(response.data.data) ? response.data.data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },

  // 스크랩한 게시글 조회
  getScrappedPosts: async (userId: number, page = 0, size = 20): Promise<Post[]> => {
    const response = await apiClient.get(
      `/posts/scraps/me?currentUserId=${userId}&page=${page}&size=${size}`
    );

    // 응답이 { success, data } 형태인지 확인
    if (response.data.success && response.data.data) {
      return Array.isArray(response.data.data) ? response.data.data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },
};
