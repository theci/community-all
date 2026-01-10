'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import { messageService } from '@/lib/services';
import { Card, Button, Input } from '@ddd3/design-system';
import type { Message } from '@ddd3/types';
import { ReportModal } from '@/components/features/report';
import { ReportTargetType } from '@ddd3/types';

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
  const [showReportModal, setShowReportModal] = useState(false);

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
    // Auto-scroll to bottom when messages change
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadMessages = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      const response = await messageService.getThreadMessages(threadId, user.id);

      // Sort messages by createdAt in ascending order (oldest first, newest last)
      const sortedMessages = (response.content || []).sort((a, b) => {
        return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      });

      setMessages(sortedMessages);

      // Determine other user from first message
      if (sortedMessages.length > 0) {
        const firstMessage = sortedMessages[0];
        const other = firstMessage.sender.id === user.id ? firstMessage.recipient : firstMessage.sender;
        setOtherUser(other);

        // Mark unread messages as read
        const unreadMessages = sortedMessages.filter(m => !m.isRead && m.recipient.id === user.id);
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
      <div className="container mx-auto px-4 py-8">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-20 bg-gray-100 rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error && messages.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
        <Button onClick={() => router.push('/messages')} className="mt-4">
          뒤로 가기
        </Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      {/* Header */}
      <div className="flex items-center gap-4 mb-6">
        <Button
          variant="ghost"
          onClick={() => router.push('/messages')}
          className="p-2"
        >
          ← 뒤로
        </Button>
        {otherUser && (
          <div className="flex items-center gap-3 flex-1 justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white font-semibold">
                {otherUser.nickname.charAt(0).toUpperCase()}
              </div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                {otherUser.nickname}
              </h1>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setShowReportModal(true)}
              className="text-red-600 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300"
            >
              <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              신고
            </Button>
          </div>
        )}
      </div>

      {error && messages.length > 0 && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* Messages Container */}
      <Card className="p-4 mb-4 min-h-[500px] max-h-[600px] overflow-y-auto">
        {messages.length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <p className="text-gray-500">대화를 시작해보세요.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {messages.map((message) => {
              const isSender = message.sender.id === user?.id;
              return (
                <div
                  key={message.id}
                  className={`flex ${isSender ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`max-w-[70%] ${isSender ? 'items-end' : 'items-start'} flex flex-col gap-1`}>
                    {!isSender && (
                      <span className="text-xs text-gray-500 px-2">
                        {message.sender.nickname}
                      </span>
                    )}
                    <div
                      className={`rounded-lg px-4 py-2 ${
                        isSender
                          ? 'bg-blue-500 text-white'
                          : 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-white'
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
            })}
            <div ref={messagesEndRef} />
          </div>
        )}
      </Card>

      {/* Message Input Form */}
      <form onSubmit={handleSendMessage} className="flex gap-2">
        <Input
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="메시지를 입력하세요..."
          className="flex-1"
          disabled={sending || !otherUser}
          maxLength={1000}
        />
        <Button
          type="submit"
          disabled={!newMessage.trim() || sending || !otherUser}
        >
          {sending ? '전송 중...' : '전송'}
        </Button>
      </form>

      {/* 신고 모달 */}
      {otherUser && (
        <ReportModal
          isOpen={showReportModal}
          onClose={() => setShowReportModal(false)}
          targetType={ReportTargetType.CHAT}
          targetId={threadId}
          targetTitle={`${otherUser.nickname}님과의 대화`}
        />
      )}
    </div>
  );
}
