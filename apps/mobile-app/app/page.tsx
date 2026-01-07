'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function MobileHome() {
  const router = useRouter();

  useEffect(() => {
    // 모바일 메인 페이지 접속 시 게시판으로 리다이렉트
    router.replace('/posts');
  }, [router]);

  // 리다이렉트 중 로딩 표시
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-black flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <p className="text-gray-600 dark:text-gray-400">로딩 중...</p>
      </div>
    </div>
  );
}
