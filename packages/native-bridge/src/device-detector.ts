import type { DeviceInfo } from './types';

/**
 * Device detection utilities
 */
export class DeviceDetector {
  private static cachedInfo: DeviceInfo | null = null;

  /**
   * Get device information
   */
  static getDeviceInfo(): DeviceInfo {
    if (this.cachedInfo) return this.cachedInfo;

    const isInApp = this.isInApp();
    const isMobile = this.isMobile();
    const userAgent = typeof navigator !== 'undefined'
      ? navigator.userAgent
      : '';

    let platform: 'ios' | 'android' | 'web' = 'web';
    if (isInApp) {
      platform = /iPad|iPhone|iPod/.test(userAgent) ? 'ios' : 'android';
    }

    this.cachedInfo = {
      isInApp,
      isMobile,
      platform,
      userAgent
    };

    return this.cachedInfo;
  }

  /**
   * Check if running inside React Native WebView
   */
  static isInApp(): boolean {
    return typeof window !== 'undefined' && !!window.ReactNativeWebView;
  }

  /**
   * Check if device is mobile
   */
  static isMobile(): boolean {
    if (typeof navigator === 'undefined') return false;
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i
      .test(navigator.userAgent);
  }

  /**
   * Determine if redirect is needed based on current host and device
   * Returns new host if redirect needed, null otherwise
   */
  static shouldRedirect(currentHost: string): string | null {
    const { isInApp, isMobile } = this.getDeviceInfo();

    // Don't redirect inside app
    if (isInApp) return null;

    const isMobileDomain = currentHost.includes('m.');
    const isWebDomain = currentHost.includes('www.');

    // PC accessing m. → redirect to www.
    if (!isMobile && isMobileDomain) {
      return currentHost.replace('m.', 'www.');
    }

    // Mobile accessing www. → redirect to m.
    if (isMobile && isWebDomain) {
      return currentHost.replace('www.', 'm.');
    }

    return null;
  }

  /**
   * Clear cached device info
   */
  static clearCache(): void {
    this.cachedInfo = null;
  }
}
