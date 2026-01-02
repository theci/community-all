'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import Link from 'next/link';
import { ThemeToggle } from '@/components/ui';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const { user, isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login?redirect=/admin');
    }
  }, [isAuthenticated, isLoading, router]);

  useEffect(() => {
    if (!isLoading && isAuthenticated && user) {
      console.log('[Admin Layout] User:', user);
      console.log('[Admin Layout] User role:', user.role);
      console.log('[Admin Layout] Role type:', typeof user.role);
      console.log('[Admin Layout] Is ADMIN?:', user.role === 'ADMIN');

      if (user.role !== 'ADMIN') {
        alert('관리자 권한이 필요합니다.');
        router.push('/');
      }
    }
  }, [user, isAuthenticated, isLoading, router]);

  if (isLoading || !isAuthenticated || !user || user.role !== 'ADMIN') {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Admin Header */}
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-8">
              <Link href="/admin" className="text-xl font-bold text-blue-600 dark:text-blue-400">
                관리자 페이지
              </Link>
              <nav className="flex gap-4">
                <Link
                  href="/admin"
                  className="text-gray-700 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 font-medium"
                >
                  대시보드
                </Link>
                <Link
                  href="/admin/users"
                  className="text-gray-700 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 font-medium"
                >
                  사용자 관리
                </Link>
                <Link
                  href="/admin/posts"
                  className="text-gray-700 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 font-medium"
                >
                  콘텐츠 관리
                </Link>
                <Link
                  href="/admin/reports"
                  className="text-gray-700 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 font-medium"
                >
                  신고 관리
                </Link>
              </nav>
            </div>
            <div className="flex items-center gap-4">
              <ThemeToggle />
              <Link
                href="/"
                className="text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100"
              >
                메인으로 돌아가기
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* Admin Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
}
