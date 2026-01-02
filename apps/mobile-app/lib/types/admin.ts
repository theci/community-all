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
