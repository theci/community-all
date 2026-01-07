'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import { messageService } from '@/lib/services';

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

      router.push(`/messages/${response.threadId}`);
    } catch (err: any) {
      setError(err.response?.data?.message || '쪽지 전송에 실패했습니다.');
      console.error('Failed to send message:', err);
      setSending(false);
    }
  };

  if (authLoading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-black p-4">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 dark:bg-gray-800 rounded w-1/4 mb-8"></div>
          <div className="h-64 bg-gray-100 dark:bg-gray-800 rounded"></div>
        </div>
      </div>
    );
  }

  if (!recipientId || !recipientName) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-black p-4">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm mb-4">
          수신자 정보가 없습니다.
        </div>
        <button
          onClick={() => router.push('/messages')}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg active:bg-blue-700"
        >
          쪽지함으로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-black">
      {/* 헤더 */}
      <div className="sticky top-0 z-50 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 px-4 py-3">
        <div className="flex items-center gap-3">
          <button
            onClick={() => router.back()}
            className="p-2 -ml-2 active:bg-gray-100 dark:active:bg-gray-800 rounded-lg"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <h1 className="text-lg font-semibold text-gray-900 dark:text-white">
            쪽지 보내기
          </h1>
        </div>
      </div>

      {error && (
        <div className="m-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      <div className="p-4">
        <form onSubmit={handleSubmit}>
          {/* 수신자 */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              받는 사람
            </label>
            <div className="flex items-center gap-3 p-3 bg-white dark:bg-gray-800 rounded-lg">
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
              className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-800 dark:text-white resize-none"
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
            <button
              type="button"
              onClick={() => router.back()}
              className="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg active:bg-gray-50 dark:active:bg-gray-800"
              disabled={sending}
            >
              취소
            </button>
            <button
              type="submit"
              className="flex-1 px-4 py-3 bg-blue-600 text-white rounded-lg active:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={!message.trim() || sending}
            >
              {sending ? '전송 중...' : '전송'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function NewMessagePage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gray-50 dark:bg-black p-4">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 dark:bg-gray-800 rounded w-1/4 mb-8"></div>
          <div className="h-64 bg-gray-100 dark:bg-gray-800 rounded"></div>
        </div>
      </div>
    }>
      <NewMessageForm />
    </Suspense>
  );
}
