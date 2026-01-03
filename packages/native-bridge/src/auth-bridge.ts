import { WebViewBridge } from './webview-bridge';
import { DeviceDetector } from './device-detector';

/**
 * Authentication synchronization between WebView and Native app
 */
export class AuthBridge {
  /**
   * Sync authentication to native app and storage
   * Called when user logs in on WebView
   */
  static async syncAuthToNative(token: string, userId: number): Promise<void> {
    const { isInApp } = DeviceDetector.getDeviceInfo();

    if (isInApp) {
      WebViewBridge.sendAuthToken(token);
    }

    // Save to Cookie (for domain sharing between web and mobile-app)
    if (typeof document !== 'undefined') {
      document.cookie = `accessToken=${token}; path=/; domain=.community.com; max-age=3600; SameSite=Lax`;
      document.cookie = `userId=${userId}; path=/; domain=.community.com; max-age=3600; SameSite=Lax`;
    }

    // Save to LocalStorage
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('accessToken', token);
      localStorage.setItem('userId', userId.toString());
    }
  }

  /**
   * Logout and clear authentication
   */
  static async logout(): Promise<void> {
    const { isInApp } = DeviceDetector.getDeviceInfo();

    if (isInApp) {
      WebViewBridge.sendLogout();
    }

    // Clear cookies
    if (typeof document !== 'undefined') {
      document.cookie = 'accessToken=; path=/; domain=.community.com; max-age=0';
      document.cookie = 'userId=; path=/; domain=.community.com; max-age=0';
    }

    // Clear LocalStorage
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('userId');
    }
  }

  /**
   * Get JavaScript injection script for native app
   * This script is injected when WebView loads
   */
  static getInjectionScript(token?: string, userId?: number): string {
    if (!token) return '';

    return `
      (function() {
        try {
          localStorage.setItem('accessToken', '${token}');
          ${userId ? `localStorage.setItem('userId', '${userId}');` : ''}

          document.cookie = 'accessToken=${token}; path=/; domain=.community.com; max-age=3600';
          ${userId ? `document.cookie = 'userId=${userId}; path=/; domain=.community.com; max-age=3600';` : ''}

          window.dispatchEvent(new Event('auth-sync'));
          console.log('[AuthBridge] Auth injected successfully');
        } catch (e) {
          console.error('[AuthBridge] Injection failed:', e);
        }
      })();
      true;
    `;
  }
}
