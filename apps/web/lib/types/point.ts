export interface PointInfo {
  id: number;
  userId: number;
  totalPoints: number;
  availablePoints: number;
  currentLevel: string; // "LEVEL_1", "LEVEL_2", etc.
  levelDisplayName: string; // "초보", "일반", etc.
  levelNumber: number;
  pointsToNextLevel: number;
  dailyEarnedPoints: number;
  remainingDailyLimit: number;
  lastEarnedDate: string;
  createdAt: string;
}

export enum PointLevel {
  LEVEL_1 = 'LEVEL_1',
  LEVEL_2 = 'LEVEL_2',
  LEVEL_3 = 'LEVEL_3',
  LEVEL_4 = 'LEVEL_4',
  LEVEL_5 = 'LEVEL_5',
}

export interface PointTransaction {
  id: number;
  userId: number;
  amount: number;
  type: PointTransactionType;
  reason: string;
  balanceAfter: number;
  createdAt: string;
}

export enum PointTransactionType {
  EARNED = 'EARNED',
  USED = 'USED',
  ADJUSTED = 'ADJUSTED',
}

export interface PointRanking {
  rank: number;
  user: {
    id: number;
    username: string;
    nickname: string;
    avatarUrl?: string;
  };
  totalPoints: number;
  currentLevel: string; // "LEVEL_1", "LEVEL_2", etc.
  levelDisplayName: string;
}
