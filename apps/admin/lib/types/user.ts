export interface User {
  id: number;
  email: string;
  username: string;
  nickname: string;
  role: UserRole;
  status: UserStatus;
  profile?: UserProfile;
  createdAt: string;
  updatedAt: string;
}

export interface UserProfile {
  bio?: string;
  avatarUrl?: string;
  website?: string;
  location?: string;
}

export enum UserRole {
  USER = 'USER',
  POWER_USER = 'POWER_USER',
  MODERATOR = 'MODERATOR',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN',
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BLOCKED = 'BLOCKED',
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
  nickname: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface UserStats {
  postCount: number;
  commentCount: number;
  likeCount: number;
  scrapCount: number;
  followerCount?: number;
  followingCount?: number;
}

export interface UserWithStats extends User {
  stats: UserStats;
  points?: number;
  level?: number;
}
