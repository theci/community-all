export interface PostLike {
  id: number;
  postId: number;
  userId: number;
  createdAt: string;
}

export interface PostScrap {
  id: number;
  postId?: number;  // 백엔드 버그: postId가 반환되지 않음
  userId?: number;
  folderId?: number;
  createdAt: string;
  post?: any;  // 백엔드 버그: post가 null로 반환됨
  scrapFolder?: any;  // 백엔드 버그: scrapFolder가 null로 반환됨
}
