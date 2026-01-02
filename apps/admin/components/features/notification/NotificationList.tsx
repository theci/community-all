'use client';

import { useState, useEffect } from 'react';
import { notificationService } from '@/lib/services';
import { useAuth } from '@/lib/hooks';
import { Button } from '@/components/ui';
import type { Notification } from '@/lib/types';
import NotificationItem from './NotificationItem';

interface NotificationListProps {
  onNotificationRead?: () => void;
}

export default function NotificationList({ onNotificationRead }: NotificationListProps) {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.id) {
      loadNotifications();
    }
  }, [user?.id]);

  const loadNotifications = async () => {
    if (!user?.id) return;

    try {
      setLoading(true);
      const response = await notificationService.getNotifications(user.id, 0, 10);
      setNotifications(Array.isArray(response.content) ? response.content : []);
    } catch (err) {
      // 백엔드 API가 아직 구현되지 않은 경우 조용히 실패
      console.warn('Notification API not available yet:', err);
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId: number) => {
    if (!user?.id) return;

    try {
      await notificationService.markAsRead(notificationId, user.id);
      setNotifications(prev =>
        prev.map(n => n.id === notificationId ? { ...n, isRead: true } : n)
      );
      if (onNotificationRead) {
        onNotificationRead();
      }
    } catch (err) {
      console.warn('Failed to mark as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (!user?.id) return;

    try {
      await notificationService.markAllAsRead(user.id);
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      if (onNotificationRead) {
        onNotificationRead();
      }
    } catch (err) {
      console.warn('Failed to mark all as read:', err);
    }
  };

  const handleDelete = async (notificationId: number) => {
    if (!user?.id) return;

    try {
      await notificationService.deleteNotification(notificationId, user.id);
      setNotifications(prev => prev.filter(n => n.id !== notificationId));
      if (onNotificationRead) {
        onNotificationRead();
      }
    } catch (err) {
      console.warn('Failed to delete notification:', err);
    }
  };

  return (
    <div className="max-h-96 overflow-y-auto">
      {/* 헤더 */}
      <div className="sticky top-0 bg-white border-b border-gray-200 p-4 flex items-center justify-between">
        <h3 className="text-lg font-semibold text-gray-900">알림</h3>
        {notifications.some(n => !n.isRead) && (
          <button
            onClick={handleMarkAllAsRead}
            className="text-sm text-blue-600 hover:text-blue-700"
          >
            모두 읽음
          </button>
        )}
      </div>

      {/* 알림 목록 */}
      <div>
        {loading ? (
          <div className="p-8 text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p className="text-sm text-gray-500 mt-2">로딩 중...</p>
          </div>
        ) : notifications.length > 0 ? (
          notifications.map((notification) => (
            <NotificationItem
              key={notification.id}
              notification={notification}
              onMarkAsRead={handleMarkAsRead}
              onDelete={handleDelete}
            />
          ))
        ) : (
          <div className="p-8 text-center">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
            <p className="text-gray-500 mt-2">알림이 없습니다</p>
          </div>
        )}
      </div>

      {/* 푸터 */}
      {notifications.length > 0 && (
        <div className="sticky bottom-0 bg-white border-t border-gray-200 p-4">
          <Button variant="ghost" fullWidth size="sm">
            모든 알림 보기
          </Button>
        </div>
      )}
    </div>
  );
}
