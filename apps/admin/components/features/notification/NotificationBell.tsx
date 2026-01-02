'use client';

import { useState, useEffect, useRef } from 'react';
import { notificationService } from '@/lib/services';
import { useAuth } from '@/lib/hooks';
import { Badge } from '@/components/ui';
import NotificationList from './NotificationList';

export default function NotificationBell() {
  const { user, isAuthenticated } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isAuthenticated && user?.id) {
      loadUnreadCount();

      // 30초마다 미읽은 알림 수 업데이트
      const interval = setInterval(loadUnreadCount, 30000);

      return () => clearInterval(interval);
    }
  }, [isAuthenticated, user?.id]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const loadUnreadCount = async () => {
    if (!user?.id) return;

    try {
      const count = await notificationService.getUnreadCount(user.id);
      setUnreadCount(count);
    } catch (err) {
      // 백엔드 API가 아직 구현되지 않은 경우 조용히 실패
      console.warn('Notification API not available yet:', err);
      setUnreadCount(0);
    }
  };

  const handleNotificationRead = () => {
    loadUnreadCount();
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-gray-600 hover:text-gray-900 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-lg"
        aria-label="알림"
      >
        <svg
          className="w-6 h-6"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
          />
        </svg>

        {unreadCount > 0 && (
          <span className="absolute top-0 right-0 transform translate-x-1/4 -translate-y-1/4">
            <Badge variant="danger" size="sm" rounded>
              {unreadCount > 99 ? '99+' : unreadCount}
            </Badge>
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-xl border border-gray-200 z-50">
          <NotificationList onNotificationRead={handleNotificationRead} />
        </div>
      )}
    </div>
  );
}
