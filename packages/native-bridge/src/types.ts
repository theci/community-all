/**
 * Message types for WebView <-> React Native communication
 */
export type MessageType =
  | 'AUTH_LOGIN'
  | 'AUTH_LOGOUT'
  | 'NAVIGATION'
  | 'SHARE'
  | 'NOTIFICATION'
  | 'DEVICE_INFO';

/**
 * WebView message structure
 */
export interface WebViewMessage<T = any> {
  type: MessageType;
  payload: T;
  timestamp: number;
}

/**
 * Device information
 */
export interface DeviceInfo {
  isInApp: boolean;
  isMobile: boolean;
  platform: 'ios' | 'android' | 'web';
  userAgent: string;
}
