import { apiClient } from '../api';
import type { Attachment } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const attachmentService = {
  // 첨부파일 업로드 (다중 파일)
  uploadAttachments: async (postId: number, files: File[], currentUserId: number): Promise<Attachment[]> => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const response = await apiClient.post<ApiResponse<Attachment[]>>(
      `/posts/${postId}/attachments?currentUserId=${currentUserId}`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  },

  // 게시글의 첨부파일 목록 조회
  getAttachments: async (postId: number): Promise<Attachment[]> => {
    const response = await apiClient.get<ApiResponse<Attachment[]>>(
      `/posts/${postId}/attachments`
    );
    return response.data.data;
  },

  // 첨부파일 삭제
  deleteAttachment: async (attachmentId: number): Promise<void> => {
    await apiClient.delete(`/posts/attachments/${attachmentId}`);
  },
};
