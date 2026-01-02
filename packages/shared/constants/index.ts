// API 엔드포인트
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

// 페이지네이션
export const DEFAULT_PAGE_SIZE = 20;
export const DEFAULT_PAGE_NUMBER = 0;

// 로컬 스토리지 키
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER: 'user',
  THEME: 'theme',
} as const;

// 라우트
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  POSTS: '/posts',
  POST_DETAIL: (id: number) => `/posts/${id}`,
  POST_CREATE: '/posts/create',
  POST_EDIT: (id: number) => `/posts/${id}/edit`,
  PROFILE: '/profile',
  ADMIN: '/admin',
  ADMIN_REPORTS: '/admin/reports',
} as const;

// 사용자 역할
export const USER_ROLES = {
  USER: 'USER',
  POWER_USER: 'POWER_USER',
  MODERATOR: 'MODERATOR',
  ADMIN: 'ADMIN',
  SUPER_ADMIN: 'SUPER_ADMIN',
} as const;

// 게시글 상태
export const POST_STATUS = {
  DRAFT: 'DRAFT',
  PUBLISHED: 'PUBLISHED',
  DELETED: 'DELETED',
  HIDDEN: 'HIDDEN',
} as const;

// 콘텐츠 타입
export const CONTENT_TYPE = {
  PLAIN_TEXT: 'PLAIN_TEXT',
  MARKDOWN: 'MARKDOWN',
  HTML: 'HTML',
} as const;
