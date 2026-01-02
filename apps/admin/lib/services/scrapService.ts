import { apiClient } from '../api';
import type { ScrapFolder, ScrapFolderCreateRequest, ScrapFolderUpdateRequest, PostScrap } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const scrapService = {
  getMyFolders: async (currentUserId: number): Promise<ScrapFolder[]> => {
    const response = await apiClient.get('/scrap-folders/me', {
      params: { currentUserId },
    });

    // 응답이 { success, data } 형태인지 확인
    if (response.data.success && response.data.data) {
      return Array.isArray(response.data.data) ? response.data.data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },

  getFolder: async (folderId: number, currentUserId: number): Promise<ScrapFolder> => {
    const response = await apiClient.get<ApiResponse<ScrapFolder>>(`/scrap-folders/${folderId}`, {
      params: { currentUserId },
    });
    return response.data.data;
  },

  createFolder: async (data: ScrapFolderCreateRequest, currentUserId: number): Promise<ScrapFolder> => {
    const response = await apiClient.post<ApiResponse<ScrapFolder>>('/scrap-folders', data, {
      params: { currentUserId },
    });
    return response.data.data;
  },

  updateFolder: async (folderId: number, data: ScrapFolderUpdateRequest, currentUserId: number): Promise<ScrapFolder> => {
    const response = await apiClient.put<ApiResponse<ScrapFolder>>(`/scrap-folders/${folderId}`, data, {
      params: { currentUserId },
    });
    return response.data.data;
  },

  deleteFolder: async (folderId: number, currentUserId: number): Promise<void> => {
    await apiClient.delete(`/scrap-folders/${folderId}`, {
      params: { currentUserId },
    });
  },

  getScrapsInFolder: async (folderId: number, currentUserId: number): Promise<PostScrap[]> => {
    const response = await apiClient.get(`/posts/scrap-folders/${folderId}/scraps`, {
      params: { currentUserId },
    });

    // 응답이 { success, data: { content, pageInfo } } 형태인지 확인
    if (response.data.success && response.data.data) {
      const data = response.data.data;

      // content 배열이 있는 경우 (페이지네이션 응답)
      if (data.content && Array.isArray(data.content)) {
        return data.content;
      }

      // 직접 배열인 경우
      return Array.isArray(data) ? data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },

  getMyScraps: async (currentUserId: number): Promise<PostScrap[]> => {
    const response = await apiClient.get('/posts/scraps/me', {
      params: { currentUserId },
    });

    // 응답이 { success, data } 형태인지 확인
    if (response.data.success && response.data.data) {
      return Array.isArray(response.data.data) ? response.data.data : [];
    }

    // 배열로 직접 반환되는 경우
    return Array.isArray(response.data) ? response.data : [];
  },

  moveScrap: async (postId: number, targetFolderId: number, currentUserId: number): Promise<void> => {
    await apiClient.put(`/posts/${postId}/scrap/move`, null, {
      params: { targetFolderId, currentUserId },
    });
  },

  setDefaultFolder: async (folderId: number, currentUserId: number): Promise<void> => {
    await apiClient.post(`/scrap-folders/${folderId}/set-default`, null, {
      params: { currentUserId },
    });
  },
};
