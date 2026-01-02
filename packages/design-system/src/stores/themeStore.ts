import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export type Theme = 'light' | 'dark' | 'system';

interface ThemeStore {
  theme: Theme;
  resolvedTheme: 'light' | 'dark';
  setTheme: (theme: Theme) => void;
  initializeTheme: () => void;
}

const getSystemTheme = (): 'light' | 'dark' => {
  if (typeof window === 'undefined') return 'light';
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

const applyTheme = (theme: 'light' | 'dark') => {
  console.log('[applyTheme] Called with theme:', theme);
  if (typeof window === 'undefined') {
    console.log('[applyTheme] Window is undefined, skipping');
    return;
  }

  const root = document.documentElement;
  console.log('[applyTheme] Current classes before:', root.className);

  if (theme === 'dark') {
    root.classList.add('dark');
    console.log('[applyTheme] Added dark class');
  } else {
    root.classList.remove('dark');
    console.log('[applyTheme] Removed dark class');
  }

  console.log('[applyTheme] Current classes after:', root.className);
};

export const useThemeStore = create<ThemeStore>()(
  persist(
    (set, get) => ({
      theme: 'system',
      resolvedTheme: 'light',

      setTheme: (theme: Theme) => {
        console.log('[themeStore] setTheme called with:', theme);
        const resolved = theme === 'system' ? getSystemTheme() : theme;
        console.log('[themeStore] Resolved theme:', resolved);
        applyTheme(resolved);
        set({ theme, resolvedTheme: resolved });
        console.log('[themeStore] State updated');
      },

      initializeTheme: () => {
        const { theme } = get();
        console.log('[themeStore] initializeTheme - current theme:', theme);
        const resolved = theme === 'system' ? getSystemTheme() : theme;
        console.log('[themeStore] initializeTheme - resolved theme:', resolved);
        applyTheme(resolved);
        set({ resolvedTheme: resolved });

        // 시스템 테마 변경 감지
        if (typeof window !== 'undefined') {
          const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
          const handleChange = (e: MediaQueryListEvent) => {
            const { theme } = get();
            if (theme === 'system') {
              const newResolved = e.matches ? 'dark' : 'light';
              applyTheme(newResolved);
              set({ resolvedTheme: newResolved });
            }
          };

          mediaQuery.addEventListener('change', handleChange);

          // Cleanup
          return () => mediaQuery.removeEventListener('change', handleChange);
        }
      },
    }),
    {
      name: 'theme-storage',
      partialize: (state) => ({ theme: state.theme }),
    }
  )
);
