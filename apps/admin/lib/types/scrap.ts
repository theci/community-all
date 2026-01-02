import type { Post } from './post';

export interface ScrapFolder {
  id: number;
  userId: number;
  name: string;
  description?: string;
  scrapCount: number;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Scrap {
  id: number;
  userId: number;
  postId: number;
  folderId: number;
  post?: Post;
  createdAt: string;
}

export interface ScrapFolderWithScraps extends ScrapFolder {
  scraps: Scrap[];
}

export interface ScrapFolderCreateRequest {
  name: string;
  description?: string;
}

export interface ScrapFolderUpdateRequest {
  name?: string;
  description?: string;
}

export interface MoveScrapRequest {
  scrapId: number;
  targetFolderId: number;
}
