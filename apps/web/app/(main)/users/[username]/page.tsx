'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';
import { userService, postService, followService } from '@/lib/services';
import { Card, Button } from '@ddd3/design-system';
import type { User, Post } from '@ddd3/types';

export default function UserPublicProfilePage() {
  const params = useParams();
  const router = useRouter();
  const { user: currentUser } = useAuth();

  // username은 실제로 userId를 전달받음
  const userId = Number(params.username);

  const [user, setUser] = useState<User | null>(null);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [followLoading, setFollowLoading] = useState(false);
  const [followCount, setFollowCount] = useState({ followingCount: 0, followerCount: 0 });

  useEffect(() => {
    if (isNaN(userId)) {
      setError('유효하지 않은 사용자 ID입니다.');
      setLoading(false);
      return;
    }

    loadUserProfile();
  }, [userId]);

  const loadUserProfile = async () => {
    try {
      setLoading(true);
      setError(null);

      // 사용자 정보 로드
      const userData = await userService.getUser(userId);
      setUser(userData);

      // 사용자의 게시글 로드
      const userPosts = await postService.getPostsByAuthor(userId);
      setPosts(userPosts);

      // 팔로우 카운트 로드
      const counts = await followService.getFollowCount(userId);
      setFollowCount(counts);

      // 현재 사용자가 로그인한 경우 팔로우 상태 확인
      if (currentUser && currentUser.id !== userId) {
        const status = await followService.getFollowStatus(userId);
        setIsFollowing(status.isFollowing);
      }
    } catch (err: any) {
      console.error('Failed to load user profile:', err);
      setError(err.response?.data?.message || '사용자 정보를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleFollowToggle = async () => {
    if (!currentUser) {
      alert('로그인이 필요합니다.');
      router.push('/login');
      return;
    }

    try {
      setFollowLoading(true);

      if (isFollowing) {
        await followService.unfollow(userId);
        setIsFollowing(false);
        setFollowCount((prev) => ({ ...prev, followerCount: prev.followerCount - 1 }));
      } else {
        await followService.follow(userId);
        setIsFollowing(true);
        setFollowCount((prev) => ({ ...prev, followerCount: prev.followerCount + 1 }));
      }
    } catch (err: any) {
      console.error('Failed to toggle follow:', err);
      alert(err.response?.data?.message || '팔로우 처리에 실패했습니다.');
    } finally {
      setFollowLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <Card className="max-w-md">
          <div className="text-center py-8">
            <p className="text-red-600 dark:text-red-400 mb-4">{error || '사용자를 찾을 수 없습니다.'}</p>
            <Button onClick={() => router.push('/')}>홈으로 돌아가기</Button>
          </div>
        </Card>
      </div>
    );
  }

  const isOwnProfile = currentUser?.id === userId;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* 프로필 헤더 */}
        <Card className="mb-8">
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-6">
              {/* 아바타 */}
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-3xl font-bold">
                {user.nickname?.charAt(0).toUpperCase() || 'U'}
              </div>

              {/* 사용자 정보 */}
              <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-1">
                  {user.nickname}
                </h1>
                <p className="text-gray-600 dark:text-gray-300 mb-2">@{user.username}</p>

                {user.profile?.bio && (
                  <p className="text-gray-700 dark:text-gray-200 mb-3 max-w-2xl">{user.profile.bio}</p>
                )}

                <div className="flex items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
                  {user.profile?.location && (
                    <div className="flex items-center gap-1">
                      <span>📍</span>
                      <span>{user.profile.location}</span>
                    </div>
                  )}
                  {user.profile?.website && (
                    <a
                      href={user.profile.website}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-center gap-1 text-blue-600 hover:text-blue-700"
                    >
                      <span>🔗</span>
                      <span>{user.profile.website}</span>
                    </a>
                  )}
                </div>
              </div>
            </div>

            {/* 본인 프로필인 경우 편집 버튼, 다른 사람 프로필인 경우 팔로우 버튼 */}
            {isOwnProfile ? (
              <div className="flex gap-2">
                <Button
                  onClick={() => router.push('/profile')}
                  className="bg-gray-200 hover:bg-gray-300 text-gray-700"
                >
                  내 프로필로
                </Button>
                <Button
                  onClick={() => router.push('/profile/edit')}
                  className="bg-blue-600 hover:bg-blue-700 text-white"
                >
                  프로필 편집
                </Button>
              </div>
            ) : currentUser ? (
              <Button
                onClick={handleFollowToggle}
                disabled={followLoading}
                className={
                  isFollowing
                    ? 'bg-gray-200 hover:bg-gray-300 text-gray-700'
                    : 'bg-blue-600 hover:bg-blue-700 text-white'
                }
              >
                {followLoading ? '처리중...' : isFollowing ? '팔로잉' : '팔로우'}
              </Button>
            ) : null}
          </div>

          {/* 통계 */}
          <div className="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
            <div className="flex gap-8">
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {posts.length}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">게시글</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {followCount.followerCount}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">팔로워</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {followCount.followingCount}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">팔로잉</div>
              </div>
            </div>
          </div>
        </Card>

        {/* 게시글 섹션 */}
        <div className="mb-6">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
            게시글 ({posts.length})
          </h2>
        </div>

        {/* 게시글 그리드 */}
        {posts.length === 0 ? (
          <Card>
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
              <p className="text-lg">아직 작성한 게시글이 없습니다.</p>
            </div>
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {posts.map((post) => (
              <Link key={post.id} href={`/posts/${post.id}`}>
                <Card className="h-full hover:shadow-lg transition-all duration-200 cursor-pointer group">
                  {/* 썸네일 영역 (이미지가 있으면 표시, 없으면 그라데이션) */}
                  <div className="aspect-video bg-gradient-to-br from-blue-100 to-purple-100 dark:from-blue-900 dark:to-purple-900 rounded-t-lg mb-4 flex items-center justify-center overflow-hidden">
                    <div className="text-4xl">📝</div>
                  </div>

                  {/* 제목 */}
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2 line-clamp-2 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                    {post.title}
                  </h3>

                  {/* 요약 */}
                  {post.summary && (
                    <p className="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
                      {post.summary}
                    </p>
                  )}

                  {/* 통계 */}
                  <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400 mt-auto pt-3 border-t border-gray-100 dark:border-gray-700">
                    <span className="flex items-center gap-1">
                      <span>👁️</span>
                      <span>{post.viewCount}</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <span>❤️</span>
                      <span>{post.likeCount}</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <span>💬</span>
                      <span>{post.commentCount}</span>
                    </span>
                  </div>

                  {/* 날짜 */}
                  <div className="text-xs text-gray-400 dark:text-gray-500 mt-2">
                    {new Date(post.createdAt).toLocaleDateString('ko-KR')}
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
