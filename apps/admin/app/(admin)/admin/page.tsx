'use client';

import { useState, useEffect } from 'react';
import { adminService } from '@/lib/services';
import type { AdminStatistics, RoleStatistics, PointStatistics } from '@/lib/types';

export default function AdminDashboardPage() {
  const [statistics, setStatistics] = useState<AdminStatistics | null>(null);
  const [roleStats, setRoleStats] = useState<RoleStatistics[]>([]);
  const [pointStats, setPointStats] = useState<PointStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    try {
      setLoading(true);
      setError(null);

      const [stats, roles, points] = await Promise.all([
        adminService.getStatistics(),
        adminService.getRoleStatistics(),
        adminService.getPointStatistics(),
      ]);

      setStatistics(stats);
      setRoleStats(roles);
      setPointStats(points);
    } catch (err: any) {
      console.error('Failed to load statistics:', err);
      setError(err.response?.data?.message || '통계를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 페이지 헤더 */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">관리자 대시보드</h1>
        <p className="mt-2 text-gray-600 dark:text-gray-400">
          커뮤니티 통계 및 현황을 확인하세요
        </p>
      </div>

      {/* 주요 지표 카드 */}
      {statistics && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <StatCard
            title="전체 사용자"
            value={statistics.totalUsers}
            subtitle={`오늘 +${statistics.newUsersToday}`}
            icon="👥"
            color="blue"
          />
          <StatCard
            title="전체 게시글"
            value={statistics.totalPosts}
            subtitle={`오늘 +${statistics.newPostsToday}`}
            icon="📝"
            color="green"
          />
          <StatCard
            title="전체 댓글"
            value={statistics.totalComments}
            subtitle="누적 댓글 수"
            icon="💬"
            color="purple"
          />
          <StatCard
            title="대기 중인 신고"
            value={statistics.pendingReports}
            subtitle={`전체 ${statistics.totalReports}건`}
            icon="⚠️"
            color="red"
          />
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 역할별 통계 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
            역할별 사용자 분포
          </h2>
          {roleStats.length > 0 ? (
            <div className="space-y-3">
              {roleStats.map((stat) => (
                <div key={stat.role} className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-2 h-2 rounded-full bg-blue-600 dark:bg-blue-500"></div>
                    <span className="text-gray-900 dark:text-white font-medium">
                      {stat.role}
                    </span>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="text-gray-600 dark:text-gray-400">{stat.count}명</span>
                    <span className="text-sm text-gray-500 dark:text-gray-500">
                      {stat.percentage.toFixed(1)}%
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-gray-500 dark:text-gray-400">데이터가 없습니다.</p>
          )}
        </div>

        {/* 포인트 통계 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
            포인트 통계
          </h2>
          {pointStats ? (
            <div className="space-y-4">
              <div className="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
                <span className="text-gray-600 dark:text-gray-400">총 포인트</span>
                <span className="text-xl font-bold text-blue-600 dark:text-blue-400">
                  {pointStats.totalPoints.toLocaleString()}P
                </span>
              </div>
              <div className="flex justify-between items-center py-2 border-b border-gray-200 dark:border-gray-700">
                <span className="text-gray-600 dark:text-gray-400">평균 포인트</span>
                <span className="text-lg font-semibold text-gray-900 dark:text-white">
                  {pointStats.averagePoints.toFixed(0)}P
                </span>
              </div>
              <div className="flex justify-between items-center py-2">
                <span className="text-gray-600 dark:text-gray-400">전체 사용자</span>
                <span className="text-lg font-semibold text-gray-900 dark:text-white">
                  {pointStats.totalUsers.toLocaleString()}명
                </span>
              </div>
            </div>
          ) : (
            <p className="text-gray-500 dark:text-gray-400">데이터가 없습니다.</p>
          )}
        </div>
      </div>

      {/* 활성 사용자 */}
      {statistics && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
            활동 현황
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <p className="text-gray-600 dark:text-gray-400 mb-2">활성 사용자</p>
              <p className="text-3xl font-bold text-green-600 dark:text-green-400">
                {statistics.activeUsers}
              </p>
            </div>
            <div className="text-center">
              <p className="text-gray-600 dark:text-gray-400 mb-2">오늘 신규 사용자</p>
              <p className="text-3xl font-bold text-blue-600 dark:text-blue-400">
                +{statistics.newUsersToday}
              </p>
            </div>
            <div className="text-center">
              <p className="text-gray-600 dark:text-gray-400 mb-2">오늘 신규 게시글</p>
              <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">
                +{statistics.newPostsToday}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// 통계 카드 컴포넌트
function StatCard({
  title,
  value,
  subtitle,
  icon,
  color,
}: {
  title: string;
  value: number;
  subtitle: string;
  icon: string;
  color: 'blue' | 'green' | 'purple' | 'red';
}) {
  const colorClasses = {
    blue: 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400',
    green: 'bg-green-50 dark:bg-green-900/20 text-green-600 dark:text-green-400',
    purple: 'bg-purple-50 dark:bg-purple-900/20 text-purple-600 dark:text-purple-400',
    red: 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400',
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-gray-600 dark:text-gray-400 text-sm font-medium">{title}</h3>
        <span className="text-2xl">{icon}</span>
      </div>
      <p className={`text-3xl font-bold mb-2 ${colorClasses[color]}`}>
        {value.toLocaleString()}
      </p>
      <p className="text-sm text-gray-500 dark:text-gray-500">{subtitle}</p>
    </div>
  );
}
