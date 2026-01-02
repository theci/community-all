'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { pointService } from '@/lib/services';
import { useAuth } from '@/lib/hooks';
import { Card } from '@/components/ui';
import type { PointRanking, PointInfo } from '@/lib/types';

export default function RankingPage() {
  const { user } = useAuth();
  const [rankings, setRankings] = useState<PointRanking[]>([]);
  const [myPoints, setMyPoints] = useState<PointInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      // 랭킹 목록 로드
      const rankingData = await pointService.getRanking(100);
      setRankings(rankingData);

      // 내 포인트 정보 로드 (로그인한 경우)
      if (user) {
        try {
          const points = await pointService.getMyPoints(user.id);
          setMyPoints(points);
        } catch (err) {
          console.error('Failed to load my points:', err);
        }
      }
    } catch (err: any) {
      console.error('Failed to load ranking:', err);
      setError(err.response?.data?.message || '랭킹을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const getLevelColor = (levelNumber: number) => {
    if (levelNumber >= 5) return 'text-blue-400';
    if (levelNumber >= 4) return 'text-purple-400';
    if (levelNumber >= 3) return 'text-yellow-500';
    if (levelNumber >= 2) return 'text-gray-400';
    return 'text-orange-600';
  };

  const getLevelBadge = (levelNumber: number) => {
    if (levelNumber >= 5) return '💎';
    if (levelNumber >= 4) return '🏆';
    if (levelNumber >= 3) return '🥇';
    if (levelNumber >= 2) return '🥈';
    return '🥉';
  };

  const getRankBadge = (rank: number) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `#${rank}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-5xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">포인트 랭킹</h1>
          <p className="text-gray-600 dark:text-gray-300">
            활동을 통해 포인트를 쌓고 레벨을 올려보세요!
          </p>
        </div>

        {error && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {/* 내 포인트 정보 */}
        {myPoints && (
          <Card className="mb-6 bg-gradient-to-r from-blue-500 to-blue-600 text-white">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold mb-1">내 포인트</h2>
                <div className="flex items-center gap-4">
                  <div>
                    <span className="text-3xl font-bold">
                      {myPoints.totalPoints?.toLocaleString() || 0}
                    </span>
                    <span className="text-blue-100 ml-2">포인트</span>
                  </div>
                  <div className="flex items-center gap-2 text-blue-100">
                    <span className="text-2xl">{getLevelBadge(myPoints.levelNumber)}</span>
                    <span className="font-medium">{myPoints.levelDisplayName}</span>
                  </div>
                </div>
              </div>
              <Link
                href="/profile"
                className="px-4 py-2 bg-white text-blue-600 rounded-lg hover:bg-blue-50 transition-colors font-medium"
              >
                프로필 보기
              </Link>
            </div>
          </Card>
        )}

        {/* 레벨 설명 */}
        <Card className="mb-6">
          <h3 className="font-semibold text-gray-900 dark:text-white mb-3">레벨 가이드</h3>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
            {[
              { level: 1, name: '초보', points: '0~99', color: 'text-orange-600' },
              { level: 2, name: '일반', points: '100~499', color: 'text-gray-400' },
              { level: 3, name: '고급', points: '500~1,999', color: 'text-yellow-500' },
              { level: 4, name: '전문가', points: '2,000~4,999', color: 'text-purple-400' },
              { level: 5, name: '마스터', points: '5,000+', color: 'text-blue-400' },
            ].map((item) => (
              <div
                key={item.level}
                className="text-center p-3 bg-gray-50 dark:bg-gray-800 rounded-lg"
              >
                <div className="text-2xl mb-1">{getLevelBadge(item.level)}</div>
                <div className={`font-medium ${item.color}`}>{item.name}</div>
                <div className="text-xs text-gray-600 dark:text-gray-400 mt-1">{item.points}</div>
              </div>
            ))}
          </div>
        </Card>

        {/* 랭킹 목록 */}
        <Card>
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">전체 랭킹</h2>

          {rankings.length === 0 ? (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <p>아직 랭킹 데이터가 없습니다.</p>
            </div>
          ) : (
            <div className="space-y-2">
              {rankings.map((ranking, index) => (
                <div
                  key={`${ranking.user.id}-${index}`}
                  className={`flex items-center justify-between p-4 rounded-lg transition-colors ${
                    ranking.user.id === user?.id
                      ? 'bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800'
                      : 'bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700'
                  }`}
                >
                  <div className="flex items-center gap-4 flex-1">
                    {/* 순위 */}
                    <div className="w-12 text-center">
                      <span className="text-xl font-bold text-gray-700 dark:text-gray-300">
                        {getRankBadge(ranking.rank)}
                      </span>
                    </div>

                    {/* 사용자 정보 */}
                    <div className="flex items-center gap-3 flex-1">
                      <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white font-bold">
                        {ranking.user.nickname?.charAt(0).toUpperCase() || 'U'}
                      </div>
                      <div>
                        <div className="font-semibold text-gray-900 dark:text-white">
                          {ranking.user.nickname}
                        </div>
                        <div className="text-sm text-gray-600 dark:text-gray-400">
                          @{ranking.user.username}
                        </div>
                      </div>
                    </div>

                    {/* 레벨 */}
                    <div className="flex items-center gap-2">
                      <span className="text-2xl">{getLevelBadge(parseInt(ranking.currentLevel.replace('LEVEL_', '')))}</span>
                      <span className={`font-medium ${getLevelColor(parseInt(ranking.currentLevel.replace('LEVEL_', '')))}`}>
                        {ranking.levelDisplayName}
                      </span>
                    </div>

                    {/* 포인트 */}
                    <div className="text-right min-w-[120px]">
                      <div className="text-2xl font-bold text-gray-900 dark:text-white">
                        {ranking.totalPoints.toLocaleString()}
                      </div>
                      <div className="text-sm text-gray-600 dark:text-gray-400">포인트</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* 포인트 획득 방법 */}
        <Card className="mt-6">
          <h3 className="font-semibold text-gray-900 dark:text-white mb-3">포인트 획득 방법</h3>
          <div className="grid md:grid-cols-2 gap-4 text-sm">
            <div className="flex items-start gap-2">
              <span className="text-green-500">✓</span>
              <div>
                <div className="font-medium text-gray-900 dark:text-white">게시글 작성</div>
                <div className="text-gray-600 dark:text-gray-400">50 포인트</div>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <span className="text-green-500">✓</span>
              <div>
                <div className="font-medium text-gray-900 dark:text-white">댓글 작성</div>
                <div className="text-gray-600 dark:text-gray-400">10 포인트</div>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <span className="text-green-500">✓</span>
              <div>
                <div className="font-medium text-gray-900 dark:text-white">좋아요 받기</div>
                <div className="text-gray-600 dark:text-gray-400">5 포인트</div>
              </div>
            </div>
            <div className="flex items-start gap-2">
              <span className="text-green-500">✓</span>
              <div>
                <div className="font-medium text-gray-900 dark:text-white">출석 체크</div>
                <div className="text-gray-600 dark:text-gray-400">10 포인트</div>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
