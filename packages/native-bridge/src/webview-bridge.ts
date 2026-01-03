import type { WebViewMessage } from './types';

declare global {
  interface Window {
    ReactNativeWebView?: {
      postMessage: (message: string) => void;
    };
  }
}

/**
 * WebView Bridge for communication between WebView and React Native
 */
export class WebViewBridge {
  private static listeners = new Map<string, Function[]>();

  /**
   * Send message from WebView to React Native
   */
  static postMessage<T>(type: string, payload: T): void {
    if (typeof window === 'undefined') return;

    if (window.ReactNativeWebView) {
      const message: WebViewMessage<T> = {
        type: type as any,
        payload,
        timestamp: Date.now()
      };
      window.ReactNativeWebView.postMessage(JSON.stringify(message));
    } else {
      console.warn('[WebViewBridge] Not in app environment');
    }
  }

  /**
   * Listen to messages from React Native
   * Returns cleanup function
   */
  static onMessage<T>(
    type: string,
    callback: (payload: T) => void
  ): () => void {
    if (typeof window === 'undefined') return () => {};

    const handler = (event: MessageEvent) => {
      try {
        const message: WebViewMessage<T> = JSON.parse(event.data);
        if (message.type === type) {
          callback(message.payload);
        }
      } catch (e) {
        console.error('[WebViewBridge] Parse error:', e);
      }
    };

    window.addEventListener('message', handler);

    // Cleanup function
    return () => {
      window.removeEventListener('message', handler);
    };
  }

  /**
   * Helper: Send authentication token to native app
   */
  static sendAuthToken(token: string): void {
    this.postMessage('AUTH_LOGIN', { token });
  }

  /**
   * Helper: Send logout notification to native app
   */
  static sendLogout(): void {
    this.postMessage('AUTH_LOGOUT', {});
  }

  /**
   * Helper: Send navigation event to native app
   */
  static sendNavigation(url: string): void {
    this.postMessage('NAVIGATION', { url });
  }
}
