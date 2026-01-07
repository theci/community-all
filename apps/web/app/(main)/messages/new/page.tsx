'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import { messageService } from '@/lib/services';
import { Card, Button, Input } from '@ddd3/design-system';

function NewMessageForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const recipientId = searchParams.get('recipientId');
  const recipientName = searchParams.get('recipientName');
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();

  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login?redirect=/messages/new');
      return;
    }

    if (!recipientId || !recipientName) {
      setError('수신자 정보가 없습니다.');
    }
  }, [authLoading, isAuthenticated, recipientId, recipientName, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user || !recipientId || !message.trim() || sending) return;

    try {
      setSending(true);
      setError(null);

      const response = await messageService.sendMessage(
        {
          recipientId: Number(recipientId),
          content: message.trim(),
        },
        user.id
      );

      // 전송 성공 후 해당 스레드로 이동
      router.push(`/messages/${response.threadId}`);
    } catch (err: any) {
      setError(err.response?.data?.message || '쪽지 전송에 실패했습니다.');
      console.error('Failed to send message:', err);
      setSending(false);
    }
  };

  if (authLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
          <div className="h-64 bg-gray-100 rounded"></div>
        </div>
      </div>
    );
  }

  if (!recipientId || !recipientName) {
    return (
      <div className="container mx-auto px-4 py-8 max-w-2xl">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
          수신자 정보가 없습니다.
        </div>
        <Button onClick={() => router.push('/messages')}>
          쪽지함으로 돌아가기
        </Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-2xl">
      <div className="mb-6">
        <Button
          variant="ghost"
          onClick={() => router.back()}
          className="p-2"
        >
          ← 뒤로
        </Button>
      </div>

      <Card className="p-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">
          쪽지 보내기
        </h1>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* 수신자 */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              받는 사람
            </label>
            <div className="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white font-semibold">
                {recipientName.charAt(0).toUpperCase()}
              </div>
              <span className="font-medium text-gray-900 dark:text-white">
                {recipientName}
              </span>
            </div>
          </div>

          {/* 메시지 내용 */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              메시지
            </label>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="메시지를 입력하세요..."
              className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white resize-none"
              rows={10}
              maxLength={1000}
              required
            />
            <div className="text-right text-sm text-gray-500 mt-1">
              {message.length} / 1000
            </div>
          </div>

          {/* 버튼 */}
          <div className="flex gap-3">
            <Button
              type="button"
              variant="ghost"
              onClick={() => router.back()}
              className="flex-1"
              disabled={sending}
            >
              취소
            </Button>
            <Button
              type="submit"
              className="flex-1"
              disabled={!message.trim() || sending}
            >
              {sending ? '전송 중...' : '전송'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}

export default function NewMessagePage() {
  return (
    <Suspense fallback={
      <div className="container mx-auto px-4 py-8">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
          <div className="h-64 bg-gray-100 rounded"></div>
        </div>
      </div>
    }>
      <NewMessageForm />
    </Suspense>
  );
}
