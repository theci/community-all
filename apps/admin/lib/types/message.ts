import { UserSummary } from './user';

export interface Message {
  id: number;
  threadId: number;
  sender: UserSummary;
  recipient: UserSummary;
  content: string;
  status: MessageStatus;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
  updatedAt: string;
  isSender: boolean;
}

export interface MessageThread {
  id: number;
  otherUser: UserSummary;
  lastMessage?: Message;
  unreadCount: number;
  lastMessageAt: string;
  createdAt: string;
}

export enum MessageStatus {
  ACTIVE = 'ACTIVE',
  DELETED = 'DELETED',
}

export interface MessageSendRequest {
  recipientId: number;
  content: string;
}
