'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAppDetection } from '@ddd3/shared-hooks';

interface TabItem {
  label: string;
  icon: React.ReactNode;
  href: string;
  activeIcon?: React.ReactNode;
}

interface BottomTabBarProps {
  tabs: TabItem[];
}

/**
 * Mobile bottom tab navigation bar
 * Automatically hidden when running inside native app
 */
export default function BottomTabBar({ tabs }: BottomTabBarProps) {
  const pathname = usePathname();
  const { isInApp } = useAppDetection();

  // Hide in app - native tab bar will be used
  if (isInApp) return null;

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 safe-area-bottom">
      <div className="flex justify-around items-center h-16">
        {tabs.map((tab) => {
          const isActive = pathname === tab.href;
          return (
            <Link
              key={tab.href}
              href={tab.href}
              className={`flex flex-col items-center justify-center flex-1 h-full transition-colors ${
                isActive
                  ? 'text-blue-600 dark:text-blue-400'
                  : 'text-gray-600 dark:text-gray-400'
              }`}
            >
              <div className="w-6 h-6 mb-1">
                {isActive && tab.activeIcon ? tab.activeIcon : tab.icon}
              </div>
              <span className="text-xs font-medium">{tab.label}</span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
