import { useEffect } from 'react';
import { AuthBridge } from '@ddd3/native-bridge';

/**
 * Hook for shared authentication across web and native app
 * Handles auth sync events and provides logout functionality
 */
export function useSharedAuth() {
  // Listen for auth sync events from native app
  useEffect(() => {
    const handleAuthSync = () => {
      const token = localStorage.getItem('accessToken');
      const userId = localStorage.getItem('userId');

      if (token && userId) {
        console.log('[useSharedAuth] Auth synced from native');
        // You can dispatch to your auth store here
        // Example: setUser({ id: parseInt(userId), token })
      }
    };

    if (typeof window !== 'undefined') {
      window.addEventListener('auth-sync', handleAuthSync);
      return () => window.removeEventListener('auth-sync', handleAuthSync);
    }
  }, []);

  const logout = async () => {
    await AuthBridge.logout();
    // You can dispatch to your auth store here
    // Example: clearUser()
  };

  return { logout };
}
