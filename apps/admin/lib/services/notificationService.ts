import { apiClient } from '../api';
import type { Notification, NotificationListResponse } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const notificationService = {
  getNotifications: async (userId: number, page = 0, size = 20): Promise<NotificationListResponse> => {
    const response = await apiClient.get<ApiResponse<NotificationListResponse>>(
      `/notifications?currentUserId=${userId}&page=${page}&size=${size}`
    );
    return response.data.data;
  },

  getUnreadCount: async (userId: number): Promise<number> => {
    const response = await apiClient.get<ApiResponse<number>>(
      `/notifications/unread-count?currentUserId=${userId}`
    );
    return response.data.data;
  },

  markAsRead: async (notificationId: number, userId: number): Promise<void> => {
    await apiClient.put(`/notifications/${notificationId}/read?currentUserId=${userId}`);
  },

  markAllAsRead: async (userId: number): Promise<void> => {
    await apiClient.put(`/notifications/read-all?currentUserId=${userId}`);
  },

  deleteNotification: async (notificationId: number, userId: number): Promise<void> => {
    await apiClient.delete(`/notifications/${notificationId}?currentUserId=${userId}`);
  },
};
