import { apiClient } from '../api';
import type { Comment, CommentCreateRequest, CommentUpdateRequest } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const commentService = {
  getCommentsByPost: async (postId: number): Promise<Comment[]> => {
    const response = await apiClient.get<ApiResponse<Comment[]>>(`/comments/posts/${postId}`);
    return response.data.data;
  },

  getRootComments: async (postId: number, page = 0, size = 20): Promise<Comment[]> => {
    const response = await apiClient.get<ApiResponse<Comment[]>>(`/comments/posts/${postId}/root`, {
      params: { page, size },
    });
    return response.data.data;
  },

  getReplies: async (parentCommentId: number): Promise<Comment[]> => {
    const response = await apiClient.get<ApiResponse<Comment[]>>(`/comments/${parentCommentId}/replies`);
    return response.data.data;
  },

  getComment: async (commentId: number): Promise<Comment> => {
    const response = await apiClient.get<ApiResponse<Comment>>(`/comments/${commentId}`);
    return response.data.data;
  },

  createComment: async (data: CommentCreateRequest, currentUserId: number): Promise<Comment> => {
    const response = await apiClient.post<ApiResponse<Comment>>(`/comments?currentUserId=${currentUserId}`, data);
    return response.data.data;
  },

  updateComment: async (commentId: number, data: CommentUpdateRequest, currentUserId: number): Promise<Comment> => {
    const response = await apiClient.put<ApiResponse<Comment>>(`/comments/${commentId}?currentUserId=${currentUserId}`, data);
    return response.data.data;
  },

  deleteComment: async (commentId: number, currentUserId: number): Promise<void> => {
    await apiClient.delete(`/comments/${commentId}?currentUserId=${currentUserId}`);
  },
};
