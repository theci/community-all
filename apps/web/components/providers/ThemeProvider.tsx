'use client';

import { useEffect } from 'react';
import { useThemeStore } from '@/lib/store/themeStore';

export default function ThemeProvider({ children }: { children: React.ReactNode }) {
  const { resolvedTheme, initializeTheme } = useThemeStore();

  // 초기 테마 설정
  useEffect(() => {
    console.log('[ThemeProvider] Initializing theme');
    initializeTheme();
  }, [initializeTheme]);

  // 테마 변경 시 적용
  useEffect(() => {
    const root = document.documentElement;
    // console.log('[ThemeProvider] Applying theme:', resolvedTheme);
    // console.log('[ThemeProvider] Current classes:', root.className);

    if (resolvedTheme === 'dark') {
      root.classList.add('dark');
      // console.log('[ThemeProvider] Added dark class');
    } else {
      root.classList.remove('dark');
      // console.log('[ThemeProvider] Removed dark class');
    }

    // console.log('[ThemeProvider] Updated classes:', root.className);
  }, [resolvedTheme]);

  return <>{children}</>;
}
