import { apiClient } from '@ddd3/api-contract';
import type { Message, MessageThread, MessageSendRequest } from '@ddd3/types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

interface PageResponse<T> {
  content: T[];
  pageInfo: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
}

export const messageService = {
  // 메시지 전송
  sendMessage: async (data: MessageSendRequest, currentUserId: number): Promise<Message> => {
    const response = await apiClient.post<ApiResponse<Message>>(
      `/messages?currentUserId=${currentUserId}`,
      data
    );
    return response.data.data;
  },

  // 메시지 조회
  getMessage: async (messageId: number, currentUserId: number): Promise<Message> => {
    const response = await apiClient.get<ApiResponse<Message>>(
      `/messages/${messageId}?currentUserId=${currentUserId}`
    );
    return response.data.data;
  },

  // 받은 메시지 목록
  getInbox: async (currentUserId: number, page = 0, size = 20): Promise<PageResponse<Message>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Message>>>(
      `/messages/inbox?currentUserId=${currentUserId}&page=${page}&size=${size}`
    );
    return response.data.data;
  },

  // 보낸 메시지 목록
  getSentMessages: async (currentUserId: number, page = 0, size = 20): Promise<PageResponse<Message>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Message>>>(
      `/messages/sent?currentUserId=${currentUserId}&page=${page}&size=${size}`
    );
    return response.data.data;
  },

  // 대화 스레드 목록
  getThreads: async (currentUserId: number, page = 0, size = 20): Promise<PageResponse<MessageThread>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<MessageThread>>>(
      `/messages/threads?currentUserId=${currentUserId}&page=${page}&size=${size}`
    );
    return response.data.data;
  },

  // 특정 스레드의 메시지 목록
  getThreadMessages: async (
    threadId: number,
    currentUserId: number,
    page = 0,
    size = 50
  ): Promise<PageResponse<Message>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Message>>>(
      `/messages/threads/${threadId}?currentUserId=${currentUserId}&page=${page}&size=${size}`
    );
    return response.data.data;
  },

  // 메시지 읽음 처리
  markAsRead: async (messageId: number, currentUserId: number): Promise<void> => {
    await apiClient.put(`/messages/${messageId}/read?currentUserId=${currentUserId}`);
  },

  // 스레드 전체 읽음 처리
  markThreadAsRead: async (threadId: number, currentUserId: number): Promise<void> => {
    await apiClient.put(`/messages/threads/${threadId}/read?currentUserId=${currentUserId}`);
  },

  // 메시지 삭제
  deleteMessage: async (messageId: number, currentUserId: number): Promise<void> => {
    await apiClient.delete(`/messages/${messageId}?currentUserId=${currentUserId}`);
  },

  // 안 읽은 메시지 수
  getUnreadCount: async (currentUserId: number): Promise<number> => {
    const response = await apiClient.get<ApiResponse<number>>(
      `/messages/unread-count?currentUserId=${currentUserId}`
    );
    return response.data.data;
  },
};
