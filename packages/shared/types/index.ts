// API 공통 타입
export interface ApiResponse<T> {
  data: T;
  message?: string;
  timestamp?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  error: string;
  message: string;
  path?: string;
  timestamp?: string;
}

// 사용자 기본 타입
export interface User {
  id: number;
  email: string;
  nickname: string;
  role: 'USER' | 'POWER_USER' | 'MODERATOR' | 'ADMIN' | 'SUPER_ADMIN';
  status: 'ACTIVE' | 'INACTIVE' | 'BLOCKED';
}

// 게시글 기본 타입
export interface Post {
  id: number;
  title: string;
  content: string;
  author: User;
  category: {
    id: number;
    name: string;
  };
  viewCount: number;
  likeCount: number;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
}
