'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import { messageService } from '@/lib/services';
import { Card, Button } from '@ddd3/design-system';
import type { MessageThread, Message } from '@ddd3/types';

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
      <div className="container mx-auto px-4 py-8">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-24 bg-gray-100 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">쪽지함</h1>
          {unreadCount > 0 && (
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
              읽지 않은 메시지 {unreadCount}개
            </p>
          )}
        </div>
      </div>

      {/* 탭 */}
      <div className="border-b border-gray-200 dark:border-gray-700 mb-6">
        <nav className="flex space-x-8">
          <button
            onClick={() => setActiveTab('threads')}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'threads'
                ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            대화 목록
          </button>
          <button
            onClick={() => setActiveTab('inbox')}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'inbox'
                ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            받은 쪽지함
            {unreadCount > 0 && (
              <span className="ml-2 bg-red-500 text-white text-xs rounded-full px-2 py-0.5">
                {unreadCount}
              </span>
            )}
          </button>
          <button
            onClick={() => setActiveTab('sent')}
            className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'sent'
                ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            보낸 쪽지함
          </button>
        </nav>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
          {error}
        </div>
      )}

      {/* 대화 목록 */}
      {activeTab === 'threads' && (
        <div className="space-y-2">
          {threads.length === 0 ? (
            <Card className="p-8 text-center">
              <p className="text-gray-500">대화 내역이 없습니다.</p>
            </Card>
          ) : (
            threads.map((thread) => (
              <Card
                key={thread.id}
                className="p-4 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
                onClick={() => handleThreadClick(thread.id)}
              >
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white font-semibold flex-shrink-0">
                    {thread.otherUser.nickname.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="font-semibold text-gray-900 dark:text-white">
                        {thread.otherUser.nickname}
                      </h3>
                      <span className="text-xs text-gray-500">
                        {new Date(thread.lastMessageAt).toLocaleDateString('ko-KR')}
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
              </Card>
            ))
          )}
        </div>
      )}

      {/* 받은 쪽지함 */}
      {activeTab === 'inbox' && (
        <div className="space-y-2">
          {inbox.length === 0 ? (
            <Card className="p-8 text-center">
              <p className="text-gray-500">받은 쪽지가 없습니다.</p>
            </Card>
          ) : (
            inbox.map((message) => (
              <Card
                key={message.id}
                className={`p-4 transition-colors ${
                  !message.isRead ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                }`}
              >
                <div className="flex items-start gap-4">
                  <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white font-semibold flex-shrink-0">
                    {message.sender.nickname.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold text-gray-900 dark:text-white">
                          {message.sender.nickname}
                        </h3>
                        {!message.isRead && (
                          <span className="bg-blue-500 text-white text-xs rounded-full px-2 py-0.5">
                            New
                          </span>
                        )}
                      </div>
                      <span className="text-xs text-gray-500">
                        {new Date(message.createdAt).toLocaleString('ko-KR')}
                      </span>
                    </div>
                    <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
                      {message.content}
                    </p>
                    {!message.isRead && (
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => handleMarkAsRead(message.id)}
                        className="mt-2"
                      >
                        읽음 표시
                      </Button>
                    )}
                  </div>
                </div>
              </Card>
            ))
          )}
        </div>
      )}

      {/* 보낸 쪽지함 */}
      {activeTab === 'sent' && (
        <div className="space-y-2">
          {sent.length === 0 ? (
            <Card className="p-8 text-center">
              <p className="text-gray-500">보낸 쪽지가 없습니다.</p>
            </Card>
          ) : (
            sent.map((message) => (
              <Card key={message.id} className="p-4">
                <div className="flex items-start gap-4">
                  <div className="w-10 h-10 rounded-full bg-gradient-to-br from-green-400 to-blue-600 flex items-center justify-center text-white font-semibold flex-shrink-0">
                    {message.recipient.nickname.charAt(0).toUpperCase()}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="font-semibold text-gray-900 dark:text-white">
                        받는 사람: {message.recipient.nickname}
                      </h3>
                      <span className="text-xs text-gray-500">
                        {new Date(message.createdAt).toLocaleString('ko-KR')}
                      </span>
                    </div>
                    <p className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap">
                      {message.content}
                    </p>
                    {message.isRead && message.readAt && (
                      <p className="text-xs text-green-600 dark:text-green-400 mt-2">
                        읽음 · {new Date(message.readAt).toLocaleString('ko-KR')}
                      </p>
                    )}
                  </div>
                </div>
              </Card>
            ))
          )}
        </div>
      )}
    </div>
  );
}
