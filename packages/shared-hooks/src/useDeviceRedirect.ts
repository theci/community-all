import { useEffect } from 'react';
import { DeviceDetector } from '@ddd3/native-bridge';

/**
 * Hook to handle automatic device-based redirects
 * Redirects PC users from m. to www. and mobile users from www. to m.
 */
export function useDeviceRedirect() {
  useEffect(() => {
    if (typeof window === 'undefined') return;

    const host = window.location.host;
    const redirectHost = DeviceDetector.shouldRedirect(host);

    if (redirectHost) {
      const newUrl = `${window.location.protocol}//${redirectHost}${window.location.pathname}${window.location.search}`;
      window.location.href = newUrl;
    }
  }, []);
}
