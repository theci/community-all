'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';
import { userService, postService, pointService } from '@/lib/services';
import { Card, Button } from '@/components/ui';
import type { UserWithStats, Post, PointInfo } from '@/lib/types';

type TabType = 'posts' | 'comments' | 'likes' | 'scraps';

export default function ProfilePage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();

  const [profile, setProfile] = useState<UserWithStats | null>(null);
  const [pointInfo, setPointInfo] = useState<PointInfo | null>(null);
  const [activeTab, setActiveTab] = useState<TabType>('posts');
  const [posts, setPosts] = useState<Post[]>([]);
  const [likedPosts, setLikedPosts] = useState<Post[]>([]);
  const [scrappedPosts, setScrappedPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login?redirect=/profile');
      return;
    }

    if (user) {
      loadProfileData();
    }
  }, [authLoading, isAuthenticated, user, router]);

  const loadProfileData = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      // 프로필 정보는 이미 authStore에 있으므로 그대로 사용
      const profileData: UserWithStats = {
        ...user,
        stats: {
          postCount: 0,
          commentCount: 0,
          likeCount: 0,
          scrapCount: 0,
        },
      };
      setProfile(profileData);

      // 포인트 정보 로드
      try {
        const points = await pointService.getMyPoints(user.id);
        setPointInfo(points);
      } catch (err) {
        console.error('Failed to load points:', err);
      }

      // 작성한 게시글 로드
      try {
        const userPosts = await postService.getPostsByAuthor(user.id);
        setPosts(userPosts);

        // 실제 게시글 수로 stats 업데이트
        setProfile((prev) => prev ? {
          ...prev,
          stats: {
            ...prev.stats,
            postCount: userPosts.length,
          },
        } : null);
      } catch (err) {
        console.error('Failed to load posts:', err);
      }

    } catch (err: any) {
      console.error('Failed to load profile:', err);
      setError(err.response?.data?.message || '프로필을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const loadLikedPosts = async () => {
    if (!user) return;
    try {
      const posts = await postService.getLikedPosts(user.id);
      setLikedPosts(posts);

      // 좋아요 수로 stats 업데이트
      setProfile((prev) => prev ? {
        ...prev,
        stats: {
          ...prev.stats,
          likeCount: posts.length,
        },
      } : null);
    } catch (err) {
      console.error('Failed to load liked posts:', err);
    }
  };

  const loadScrappedPosts = async () => {
    if (!user) return;
    try {
      const posts = await postService.getScrappedPosts(user.id);
      setScrappedPosts(posts);

      // 스크랩 수로 stats 업데이트
      setProfile((prev) => prev ? {
        ...prev,
        stats: {
          ...prev.stats,
          scrapCount: posts.length,
        },
      } : null);
    } catch (err) {
      console.error('Failed to load scrapped posts:', err);
    }
  };

  useEffect(() => {
    if (!user) return;

    if (activeTab === 'likes' && likedPosts.length === 0) {
      loadLikedPosts();
    } else if (activeTab === 'scraps' && scrappedPosts.length === 0) {
      loadScrappedPosts();
    }
  }, [activeTab, user]);

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
      </div>
    );
  }

  if (!isAuthenticated || !profile) {
    return null;
  }

  const tabButtons: { key: TabType; label: string; count?: number }[] = [
    { key: 'posts', label: '게시글', count: profile.stats?.postCount },
    { key: 'comments', label: '댓글', count: profile.stats?.commentCount },
    { key: 'likes', label: '좋아요', count: profile.stats?.likeCount },
    { key: 'scraps', label: '스크랩', count: profile.stats?.scrapCount },
  ];

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-6xl mx-auto px-4 py-8">
        {error && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {/* 프로필 헤더 */}
        <Card className="mb-6">
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-6">
              {/* 아바타 */}
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-3xl font-bold">
                {profile.nickname?.charAt(0).toUpperCase() || 'U'}
              </div>

              {/* 사용자 정보 */}
              <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-1">
                  {profile.nickname}
                </h1>
                <p className="text-gray-600 dark:text-gray-300 mb-2">@{profile.username}</p>

                {profile.profile?.bio && (
                  <p className="text-gray-700 dark:text-gray-200 mb-3">{profile.profile.bio}</p>
                )}

                <div className="flex items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
                  {profile.profile?.location && (
                    <div className="flex items-center gap-1">
                      <span>📍</span>
                      <span>{profile.profile.location}</span>
                    </div>
                  )}
                  {profile.profile?.website && (
                    <a
                      href={profile.profile.website}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-center gap-1 text-blue-600 hover:text-blue-700"
                    >
                      <span>🔗</span>
                      <span>{profile.profile.website}</span>
                    </a>
                  )}
                </div>
              </div>
            </div>

            {/* 프로필 편집 버튼 */}
            <Button
              onClick={() => router.push('/profile/edit')}
              className="bg-gray-200 hover:bg-gray-300 text-gray-700"
            >
              프로필 편집
            </Button>
          </div>

          {/* 통계 및 포인트 */}
          <div className="mt-6 pt-6 border-t border-gray-200">
            <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {profile.stats?.postCount || 0}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">게시글</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {profile.stats?.commentCount || 0}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">댓글</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {profile.stats?.likeCount || 0}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">좋아요</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {profile.stats?.scrapCount || 0}
                </div>
                <div className="text-sm text-gray-600 dark:text-gray-400">스크랩</div>
              </div>
              {pointInfo && (
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">
                    {pointInfo.totalPoints?.toLocaleString() || 0}
                  </div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">
                    포인트 ({pointInfo.levelDisplayName})
                  </div>
                </div>
              )}
            </div>
          </div>
        </Card>

        {/* 탭 네비게이션 */}
        <div className="mb-6 border-b border-gray-200">
          <div className="flex gap-1">
            {tabButtons.map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`px-6 py-3 font-medium text-sm border-b-2 transition-colors ${
                  activeTab === tab.key
                    ? 'border-blue-600 text-blue-600 dark:text-blue-400'
                    : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:border-gray-300 dark:hover:border-gray-600'
                }`}
              >
                {tab.label}
                {tab.count !== undefined && (
                  <span className="ml-2 text-gray-500">({tab.count})</span>
                )}
              </button>
            ))}
          </div>
        </div>

        {/* 탭 콘텐츠 */}
        <div>
          {activeTab === 'posts' && (
            <div>
              {posts.length === 0 ? (
                <Card>
                  <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                    <p className="text-lg mb-2">작성한 게시글이 없습니다.</p>
                    <Link href="/posts/create">
                      <Button className="mt-4">게시글 작성하기</Button>
                    </Link>
                  </div>
                </Card>
              ) : (
                <div className="space-y-4">
                  {posts.map((post) => (
                    <Card key={post.id} className="hover:shadow-md transition-shadow">
                      <Link href={`/posts/${post.id}`}>
                        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2 hover:text-blue-600 dark:hover:text-blue-400">
                          {post.title}
                        </h3>
                        {post.summary && (
                          <p className="text-gray-600 dark:text-gray-300 mb-3 line-clamp-2">{post.summary}</p>
                        )}
                        <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                          <span>👁️ {post.viewCount}</span>
                          <span>❤️ {post.likeCount}</span>
                          <span>💬 {post.commentCount}</span>
                          <span>{new Date(post.createdAt).toLocaleDateString()}</span>
                        </div>
                      </Link>
                    </Card>
                  ))}
                </div>
              )}
            </div>
          )}

          {activeTab === 'comments' && (
            <Card>
              <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                <p>댓글 목록 기능은 곧 제공될 예정입니다.</p>
              </div>
            </Card>
          )}

          {activeTab === 'likes' && (
            <div>
              {likedPosts.length === 0 ? (
                <Card>
                  <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                    <p>좋아요한 게시글이 없습니다.</p>
                  </div>
                </Card>
              ) : (
                <div className="space-y-4">
                  {likedPosts.map((post) => (
                    <Card key={post.id} className="hover:shadow-md transition-shadow">
                      <Link href={`/posts/${post.id}`}>
                        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2 hover:text-blue-600 dark:hover:text-blue-400">
                          {post.title}
                        </h3>
                        <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                          <span>👁️ {post.viewCount}</span>
                          <span>❤️ {post.likeCount}</span>
                          <span>💬 {post.commentCount}</span>
                        </div>
                      </Link>
                    </Card>
                  ))}
                </div>
              )}
            </div>
          )}

          {activeTab === 'scraps' && (
            <div>
              {scrappedPosts.length === 0 ? (
                <Card>
                  <div className="text-center py-12 text-gray-500 dark:text-gray-400">
                    <p className="mb-4">스크랩한 게시글이 없습니다.</p>
                    <Link href="/scraps">
                      <Button>스크랩 폴더 관리</Button>
                    </Link>
                  </div>
                </Card>
              ) : (
                <div className="space-y-4">
                  {scrappedPosts.map((post) => (
                    <Card key={post.id} className="hover:shadow-md transition-shadow">
                      <Link href={`/posts/${post.id}`}>
                        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2 hover:text-blue-600 dark:hover:text-blue-400">
                          {post.title}
                        </h3>
                        <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                          <span>👁️ {post.viewCount}</span>
                          <span>❤️ {post.likeCount}</span>
                          <span>💬 {post.commentCount}</span>
                        </div>
                      </Link>
                    </Card>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
