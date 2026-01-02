export interface Post {
  id: number;
  title: string;
  content: string;
  summary?: string;
  author: PostAuthor;
  category?: Category;
  tags: Tag[];
  status: PostStatus;
  viewCount: number;
  likeCount: number;
  scrapCount: number;
  commentCount: number;
  isNotice: boolean;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PostAuthor {
  id: number;
  username: string;
  nickname: string;
  avatarUrl?: string;
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
  parentId?: number;
  children?: Category[];
}

export interface Tag {
  id: number;
  name: string;
  slug: string;
  usageCount: number;
}

export enum PostStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  DELETED = 'DELETED',
}

export interface PostCreateRequest {
  title: string;
  content: string;
  contentType: 'MARKDOWN' | 'HTML' | 'PLAIN_TEXT';
  categoryId: number;  // 필수!
  summary?: string;
  tags?: string[];
}

export interface PostUpdateRequest {
  title?: string;
  content?: string;
  contentType?: 'MARKDOWN' | 'HTML' | 'PLAIN_TEXT';
  summary?: string;
  categoryId?: number;
  tags?: string[];
}

export interface PostSearchRequest {
  keyword?: string;
  categoryId?: number;
  tagIds?: number[];
  authorId?: number;
  startDate?: string;
  endDate?: string;
  status?: PostStatus;
}

export interface PageInfo {
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface PostListResponse {
  content: Post[];
  pageInfo: PageInfo;
}
