'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import { messageService } from '@/lib/services';
import type { MessageThread, Message } from '@/lib/types';

type TabType = 'threads' | 'inbox' | 'sent';

export default function MessagesPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();

  const [activeTab, setActiveTab] = useState<TabType>('threads');
  const [threads, setThreads] = useState<MessageThread[]>([]);
  const [inbox, setInbox] = useState<Message[]>([]);
  const [sent, setSent] = useState<Message[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login?redirect=/messages');
      return;
    }

    if (user) {
      loadMessages();
      loadUnreadCount();
    }
  }, [authLoading, isAuthenticated, user, router, activeTab]);

  const loadMessages = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      if (activeTab === 'threads') {
        const response = await messageService.getThreads(user.id);
        setThreads(response.content || []);
      } else if (activeTab === 'inbox') {
        const response = await messageService.getInbox(user.id);
        setInbox(response.content || []);
      } else if (activeTab === 'sent') {
        const response = await messageService.getSentMessages(user.id);
        setSent(response.content || []);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || '메시지를 불러오는데 실패했습니다.');
      console.error('Failed to load messages:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadUnreadCount = async () => {
    if (!user) return;
    try {
      const count = await messageService.getUnreadCount(user.id);
      setUnreadCount(count);
    } catch (err) {
      console.error('Failed to load unread count:', err);
    }
  };

  const handleThreadClick = (threadId: number) => {
    router.push(`/messages/${threadId}`);
  };

  const handleMarkAsRead = async (messageId: number) => {
    if (!user) return;
    try {
      await messageService.markAsRead(messageId, user.id);
      loadMessages();
      loadUnreadCount();
    } catch (err) {
      console.error('Failed to mark as read:', err);
    }
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-black p-4">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 dark:bg-gray-800 rounded w-1/4"></div>
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-24 bg-gray-100 dark:bg-gray-800 rounded"></div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-black">
      {/* 헤더 */}
      <div className="sticky top-0 z-50 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 px-4 py-3">
        <div className="flex items-center justify-between">
          <h1 className="text-xl font-bold text-gray-900 dark:text-white">쪽지함</h1>
          {unreadCount > 0 && (
            <span className="bg-red-500 text-white text-xs rounded-full px-2 py-1">
              {unreadCount}
            </span>
          )}
        </div>
      </div>

      {/* 탭 */}
      <div className="sticky top-[57px] z-40 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
        <div className="flex">
          <button
            onClick={() => setActiveTab('threads')}
            className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${
              activeTab === 'threads'
                ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                : 'border-transparent text-gray-500 dark:text-gray-400'
            }`}
          >
            대화
          </button>
          <button
            onClick={() => setActiveTab('inbox')}
            className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${
              activeTab === 'inbox'
                ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                : 'border-transparent text-gray-500 dark:text-gray-400'
            }`}
          >
            받은함
            {unreadCount > 0 && (
              <span className="ml-1 bg-red-500 text-white text-xs rounded-full px-1.5">
                {unreadCount}
              </span>
            )}
          </button>
          <button
            onClick={() => setActiveTab('sent')}
            className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${
              activeTab === 'sent'
                ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                : 'border-transparent text-gray-500 dark:text-gray-400'
            }`}
          >
            보낸함
          </button>
        </div>
      </div>

      {error && (
        <div className="m-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      <div className="p-4">
        {/* 대화 목록 */}
        {activeTab === 'threads' && (
          <div className="space-y-2">
            {threads.length === 0 ? (
              <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                <p>대화 내역이 없습니다.</p>
              </div>
            ) : (
              threads.map((thread) => (
                <div
                  key={thread.id}
                  onClick={() => handleThreadClick(thread.id)}
                  className="bg-white dark:bg-gray-800 rounded-lg p-4 active:bg-gray-50 dark:active:bg-gray-700"
                >
                  <div className="flex items-start gap-3">
                    <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white font-semibold flex-shrink-0">
                      {thread.otherUser.nickname.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <h3 className="font-semibold text-gray-900 dark:text-white text-sm">
                          {thread.otherUser.nickname}
                        </h3>
                        <span className="text-xs text-gray-500">
                          {new Date(thread.lastMessageAt).toLocaleDateString('ko-KR', {
                            month: 'short',
                            day: 'numeric',
                          })}
                        </span>
                      </div>
                      {thread.lastMessage && (
                        <p className="text-sm text-gray-600 dark:text-gray-400 truncate">
                          {thread.lastMessage.content}
                        </p>
                      )}
                      {thread.unreadCount > 0 && (
                        <span className="inline-block mt-2 bg-red-500 text-white text-xs rounded-full px-2 py-0.5">
                          {thread.unreadCount}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* 받은 쪽지함 */}
        {activeTab === 'inbox' && (
          <div className="space-y-2">
            {inbox.length === 0 ? (
              <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                <p>받은 쪽지가 없습니다.</p>
              </div>
            ) : (
              inbox.map((message) => (
                <div
                  key={message.id}
                  className={`bg-white dark:bg-gray-800 rounded-lg p-4 ${
                    !message.isRead ? 'border-l-4 border-blue-500' : ''
                  }`}
                >
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white font-semibold flex-shrink-0">
                      {message.sender.nickname.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <h3 className="font-semibold text-gray-900 dark:text-white text-sm">
                            {message.sender.nickname}
                          </h3>
                          {!message.isRead && (
                            <span className="bg-blue-500 text-white text-xs rounded-full px-2 py-0.5">
                              New
                            </span>
                          )}
                        </div>
                        <span className="text-xs text-gray-500">
                          {new Date(message.createdAt).toLocaleDateString('ko-KR', {
                            month: 'short',
                            day: 'numeric',
                          })}
                        </span>
                      </div>
                      <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap break-words">
                        {message.content}
                      </p>
                      {!message.isRead && (
                        <button
                          onClick={() => handleMarkAsRead(message.id)}
                          className="mt-2 text-xs text-blue-600 dark:text-blue-400 active:opacity-70"
                        >
                          읽음 표시
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* 보낸 쪽지함 */}
        {activeTab === 'sent' && (
          <div className="space-y-2">
            {sent.length === 0 ? (
              <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                <p>보낸 쪽지가 없습니다.</p>
              </div>
            ) : (
              sent.map((message) => (
                <div key={message.id} className="bg-white dark:bg-gray-800 rounded-lg p-4">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-green-400 to-blue-600 flex items-center justify-center text-white font-semibold flex-shrink-0">
                      {message.recipient.nickname.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-2">
                        <h3 className="font-semibold text-gray-900 dark:text-white text-sm">
                          받는 사람: {message.recipient.nickname}
                        </h3>
                        <span className="text-xs text-gray-500">
                          {new Date(message.createdAt).toLocaleDateString('ko-KR', {
                            month: 'short',
                            day: 'numeric',
                          })}
                        </span>
                      </div>
                      <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap break-words">
                        {message.content}
                      </p>
                      {message.isRead && message.readAt && (
                        <p className="text-xs text-green-600 dark:text-green-400 mt-2">
                          읽음 · {new Date(message.readAt).toLocaleDateString('ko-KR')}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}
