'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import { messageService } from '@/lib/services';
import type { Message } from '@/lib/types';

export default function MessageThreadPage() {
  const router = useRouter();
  const params = useParams();
  const threadId = Number(params.threadId);
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();

  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [otherUser, setOtherUser] = useState<{ id: number; nickname: string } | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login?redirect=/messages');
      return;
    }

    if (user && threadId) {
      loadMessages();
    }
  }, [authLoading, isAuthenticated, user, threadId, router]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadMessages = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      const response = await messageService.getThreadMessages(threadId, user.id);
      setMessages(response.content || []);

      if (response.content && response.content.length > 0) {
        const firstMessage = response.content[0];
        const other = firstMessage.sender.id === user.id ? firstMessage.recipient : firstMessage.sender;
        setOtherUser(other);

        const unreadMessages = response.content.filter(m => !m.isRead && m.recipient.id === user.id);
        for (const msg of unreadMessages) {
          await messageService.markAsRead(msg.id, user.id);
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.message || '메시지를 불러오는데 실패했습니다.');
      console.error('Failed to load messages:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !newMessage.trim() || !otherUser || sending) return;

    try {
      setSending(true);
      const message = await messageService.sendMessage(
        {
          recipientId: otherUser.id,
          content: newMessage.trim(),
        },
        user.id
      );

      setMessages([...messages, message]);
      setNewMessage('');
    } catch (err: any) {
      setError(err.response?.data?.message || '메시지 전송에 실패했습니다.');
      console.error('Failed to send message:', err);
    } finally {
      setSending(false);
    }
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-black p-4">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 dark:bg-gray-800 rounded w-1/4"></div>
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-20 bg-gray-100 dark:bg-gray-800 rounded"></div>
          ))}
        </div>
      </div>
    );
  }

  if (error && messages.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-black p-4">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
        <button
          onClick={() => router.push('/messages')}
          className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg active:bg-blue-700"
        >
          뒤로 가기
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-black">
      {/* 헤더 */}
      <div className="sticky top-0 z-50 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 px-4 py-3">
        <div className="flex items-center gap-3">
          <button
            onClick={() => router.push('/messages')}
            className="p-2 -ml-2 active:bg-gray-100 dark:active:bg-gray-800 rounded-lg"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          {otherUser && (
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white font-semibold text-sm">
                {otherUser.nickname.charAt(0).toUpperCase()}
              </div>
              <h1 className="text-lg font-semibold text-gray-900 dark:text-white">
                {otherUser.nickname}
              </h1>
            </div>
          )}
        </div>
      </div>

      {error && messages.length > 0 && (
        <div className="mx-4 mt-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      {/* 메시지 목록 */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {messages.length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <p className="text-gray-500 dark:text-gray-400">대화를 시작해보세요.</p>
          </div>
        ) : (
          messages.map((message) => {
            const isSender = message.sender.id === user?.id;
            return (
              <div
                key={message.id}
                className={`flex ${isSender ? 'justify-end' : 'justify-start'}`}
              >
                <div className={`max-w-[75%] ${isSender ? 'items-end' : 'items-start'} flex flex-col gap-1`}>
                  {!isSender && (
                    <span className="text-xs text-gray-500 px-2">
                      {message.sender.nickname}
                    </span>
                  )}
                  <div
                    className={`rounded-2xl px-4 py-2 ${
                      isSender
                        ? 'bg-blue-500 text-white'
                        : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-white'
                    }`}
                  >
                    <p className="text-sm whitespace-pre-wrap break-words">
                      {message.content}
                    </p>
                  </div>
                  <div className="flex items-center gap-2 px-2">
                    <span className="text-xs text-gray-400">
                      {new Date(message.createdAt).toLocaleString('ko-KR', {
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                    {isSender && message.isRead && (
                      <span className="text-xs text-blue-400">읽음</span>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 입력 폼 */}
      <div className="sticky bottom-0 bg-white dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800 p-4 safe-area-bottom">
        <form onSubmit={handleSendMessage} className="flex gap-2">
          <textarea
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="메시지를 입력하세요..."
            className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-800 dark:text-white resize-none"
            rows={1}
            maxLength={1000}
            disabled={sending || !otherUser}
          />
          <button
            type="submit"
            disabled={!newMessage.trim() || sending || !otherUser}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg active:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {sending ? '전송중' : '전송'}
          </button>
        </form>
      </div>
    </div>
  );
}
