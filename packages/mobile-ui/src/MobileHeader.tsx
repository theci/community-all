'use client';

import { useRouter } from 'next/navigation';
import { useAppDetection } from '@ddd3/shared-hooks';

interface MobileHeaderProps {
  title: string;
  showBack?: boolean;
  rightAction?: React.ReactNode;
}

/**
 * Mobile page header
 * Automatically hidden when running inside native app
 */
export default function MobileHeader({
  title,
  showBack = false,
  rightAction
}: MobileHeaderProps) {
  const router = useRouter();
  const { isInApp } = useAppDetection();

  // Hide in app - native header will be used
  if (isInApp) return null;

  return (
    <header className="sticky top-0 z-50 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div className="flex items-center justify-between h-14 px-4">
        {showBack ? (
          <button
            onClick={() => router.back()}
            className="p-2 -ml-2 text-gray-700 dark:text-gray-300"
            aria-label="뒤로가기"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
        ) : (
          <div className="w-10" />
        )}

        <h1 className="flex-1 text-lg font-semibold text-center text-gray-900 dark:text-gray-100">
          {title}
        </h1>

        <div className="w-10">
          {rightAction}
        </div>
      </div>
    </header>
  );
}
