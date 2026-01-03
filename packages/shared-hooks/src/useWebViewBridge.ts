import { useEffect, useCallback } from 'react';
import { WebViewBridge } from '@ddd3/native-bridge';
import type { MessageType } from '@ddd3/native-bridge';

/**
 * Hook to communicate with React Native WebView
 * Provides message sending and receiving capabilities
 */
export function useWebViewBridge<T = any>(
  messageType: MessageType,
  onMessage: (payload: T) => void
) {
  useEffect(() => {
    const cleanup = WebViewBridge.onMessage<T>(messageType, onMessage);
    return cleanup;
  }, [messageType, onMessage]);

  const postMessage = useCallback((payload: T) => {
    WebViewBridge.postMessage(messageType, payload);
  }, [messageType]);

  return { postMessage };
}
