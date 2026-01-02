export type NotificationType =
  | 'COMMENT'
  | 'REPLY'
  | 'LIKE'
  | 'SCRAP'
  | 'FOLLOW'
  | 'MENTION'
  | 'SYSTEM';

export interface Notification {
  id: number;
  userId: number;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  relatedId?: number;
  relatedType?: 'POST' | 'COMMENT' | 'USER';
  createdAt: string;
  readAt?: string;
}

export interface NotificationListResponse {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
