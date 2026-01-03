import { useState, useEffect } from 'react';
import { DeviceDetector } from '@ddd3/native-bridge';
import type { DeviceInfo } from '@ddd3/native-bridge';

/**
 * Hook to detect device and app environment
 * Returns device information including whether running in app, on mobile, and platform
 */
export function useAppDetection() {
  const [deviceInfo, setDeviceInfo] = useState<DeviceInfo>(() => ({
    isInApp: false,
    isMobile: false,
    platform: 'web',
    userAgent: ''
  }));

  useEffect(() => {
    // Skip in SSR environment
    if (typeof window === 'undefined') return;

    const info = DeviceDetector.getDeviceInfo();
    setDeviceInfo(info);
  }, []);

  return deviceInfo;
}
