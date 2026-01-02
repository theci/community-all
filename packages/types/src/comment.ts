export interface Comment {
  id: number;
  content: string;
  author: CommentAuthor;
  postId: number;
  parentId?: number;
  replies?: Comment[];
  replyCount: number;
  status: CommentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CommentAuthor {
  id: number;
  username: string;
  nickname: string;
  avatarUrl?: string;
}

export enum CommentStatus {
  ACTIVE = 'ACTIVE',
  DELETED = 'DELETED',
  BLOCKED = 'BLOCKED',
}

export interface CommentCreateRequest {
  postId: number;
  parentId?: number;
  content: string;
}

export interface CommentUpdateRequest {
  content: string;
}
