// Admin-related type definitions

export interface AdminStatistics {
  totalUsers: number;
  totalPosts: number;
  totalComments: number;
  totalReports: number;
  activeUsers: number;
  newUsersToday: number;
  newPostsToday: number;
  pendingReports: number;
}

export interface RoleStatistics {
  role: string;
  count: number;
  percentage: number;
}

export interface PointStatistics {
  totalPoints: number;
  averagePoints: number;
  totalUsers: number;
}

export interface Report {
  id: number;
  reportType: 'POST' | 'COMMENT' | 'USER';
  targetId: number;
  targetType: string;
  reason: string;
  description?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  reporter: {
    id: number;
    username: string;
    nickname?: string;
  };
  reportedUser?: {
    id: number;
    username: string;
    nickname?: string;
  };
  createdAt: string;
  processedAt?: string;
  processedBy?: {
    id: number;
    username: string;
    nickname?: string;
  };
  processingNote?: string;
}

export interface ReportDetail extends Report {
  targetContent?: {
    id: number;
    content: string;
    author: {
      id: number;
      username: string;
      nickname?: string;
    };
  };
}

export interface ProcessReportRequest {
  status: 'APPROVED' | 'REJECTED';
  processingNote?: string;
  applyActions?: boolean;
}

export interface Sanction {
  id: number;
  userId: number;
  sanctionType: 'WARNING' | 'TEMPORARY_BAN' | 'PERMANENT_BAN';
  reason: string;
  description?: string;
  startDate: string;
  endDate?: string;
  isActive: boolean;
  createdBy: {
    id: number;
    username: string;
    nickname?: string;
  };
  createdAt: string;
}

export interface CreateSanctionRequest {
  userId: number;
  sanctionType: 'WARNING' | 'TEMPORARY_BAN' | 'PERMANENT_BAN';
  reason: string;
  description?: string;
  durationDays?: number;
}

// User Management types
// Note: UserStatus and UserRole are imported from user.ts to avoid duplication

export interface UserManagement {
  userId: number;
  email: string;
  nickname: string;
  status: import('./user').UserStatus;
  role: import('./user').UserRole;
  currentPoints: number;
  currentLevel: number;
  createdAt: string;
  lastLoginAt?: string;
  totalPosts: number;
  totalComments: number;
}

export interface UserDetail {
  userId: number;
  email: string;
  nickname: string;
  status: import('./user').UserStatus;
  role: import('./user').UserRole;
  currentPoints: number;
  currentLevel: number;
  createdAt: string;
  lastLoginAt?: string;
  bio?: string;
  avatarUrl?: string;
  totalPosts: number;
  totalComments: number;
  totalLikes: number;
  totalScraps: number;
  sanctions: SanctionInfo[];
}

export interface SanctionInfo {
  sanctionId: number;
  type: string;
  reason: string;
  startDate: string;
  endDate?: string;
  status: string;
}

export interface UpdateUserStatusRequest {
  status: import('./user').UserStatus;
  reason?: string;
}

export interface UpdateUserRoleRequest {
  role: import('./user').UserRole;
}

export interface AdjustUserPointRequest {
  points: number;
  reason: string;
}

// Content Moderation types
export interface PostManagement {
  postId: number;
  title: string;
  summary: string;
  authorNickname: string;
  authorId: number;
  categoryName: string;
  status: import('./post').PostStatus;
  isNoticePost: boolean;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  scrapCount: number;
  publishedAt?: string;
  createdAt: string;
  deletedAt?: string;
  deletedBy?: number;
}

export interface UpdatePostStatusRequest {
  status: import('./post').PostStatus;
  reason?: string;
}

export interface MarkAsNoticeRequest {
  isNotice: boolean;
}

export enum BulkActionType {
  DELETE = 'DELETE',
  RESTORE = 'RESTORE',
  MARK_AS_NOTICE = 'MARK_AS_NOTICE',
  UNMARK_AS_NOTICE = 'UNMARK_AS_NOTICE',
}

export interface BulkPostActionRequest {
  postIds: number[];
  action: BulkActionType;
  reason?: string;
}
