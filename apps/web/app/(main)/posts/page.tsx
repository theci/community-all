'use client';

import { useState, useEffect } from 'react';
import { postService, categoryService, userService, followService } from '@/lib/services';
import { Post, Category, User } from '@ddd3/types';
import { PostCard } from '@/components/features/post/PostCard';
import { Button } from '@ddd3/design-system';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';

export default function PostsPage() {
  const { user, isAuthenticated } = useAuth();
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortBy, setSortBy] = useState<'latest' | 'popular' | 'trending' | 'following'>('latest');
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [suggestedUsers, setSuggestedUsers] = useState<User[]>([]);
  const [followingUsers, setFollowingUsers] = useState<Set<number>>(new Set());
  const [showEmptyFollowing, setShowEmptyFollowing] = useState(false);

  const loadPosts = async (currentPage: number, sort: string, categoryId?: number | null) => {
    try {
      setLoading(true);
      setError(null);
      setShowEmptyFollowing(false);

      let response;

      // 카테고리 필터가 있는 경우
      if (categoryId) {
        response = await postService.searchPosts({ categoryId }, currentPage, 20);
        setPosts(response?.content || []);
        setTotalPages(response?.pageInfo?.totalPages || 0);
      } else {
        // 일반 목록 조회
        if (sort === 'latest') {
          response = await postService.getPosts(currentPage, 20, 'createdAt,desc');
          setPosts(response?.content || []);
          setTotalPages(response?.pageInfo?.totalPages || 0);
        } else if (sort === 'popular') {
          response = await postService.getPopularPosts(7, currentPage, 20);
          setPosts(response?.content || []);
          setTotalPages(response?.pageInfo?.totalPages || 0);
        } else if (sort === 'trending') {
          response = await postService.getTrendingPosts(24, currentPage, 20);
          setPosts(response?.content || []);
          setTotalPages(response?.pageInfo?.totalPages || 0);
        } else if (sort === 'following' && user) {
          // 팔로잉 피드
          try {
            console.log('팔로잉 피드 조회 시작...');
            console.log('현재 사용자:', user);
            console.log('isAuthenticated:', isAuthenticated);

            // 쿠키 확인
            console.log('쿠키:', document.cookie);

            response = await postService.getFollowingFeed(currentPage, 20);
            console.log('팔로잉 피드 응답:', response);
            setPosts(response?.content || []);
            setTotalPages(response?.pageInfo?.totalPages || 0);

            // 팔로잉 피드가 비어있으면 추천 사용자 로드
            if (!response?.content || response.content.length === 0) {
              console.log('팔로잉 피드가 비어있습니다. 팔로잉 목록 확인 중...');
              // 실제로 팔로잉이 있는지 확인
              const followingIds = await followService.getFollowingList(user.id);
              console.log('팔로잉 목록:', followingIds);

              if (followingIds && followingIds.length > 0) {
                // 팔로잉은 있지만 게시물이 없는 경우
                setShowEmptyFollowing(false);
                // 메시지를 다르게 표시
                setError('팔로우한 사용자들이 아직 게시글을 작성하지 않았습니다.');
              } else {
                // 정말로 팔로잉이 없는 경우
                setShowEmptyFollowing(true);
                await loadSuggestedUsers();
              }
            }
          } catch (err: any) {
            console.error('팔로잉 피드 조회 에러:', err);
            console.error('에러 상세:', err.response?.data);
            console.error('에러 상태 코드:', err.response?.status);

            // 403 에러 처리 - 인증 문제일 가능성 확인
            if (err.response?.status === 403) {
              const cookies = document.cookie;
              console.error('현재 쿠키:', cookies);

              // 쿠키가 없거나 인증 토큰이 없으면 로그인 페이지로 리다이렉트
              if (!cookies || cookies.trim() === '') {
                alert('로그인 세션이 만료되었습니다. 다시 로그인해주세요.');
                localStorage.removeItem('auth-storage');
                window.location.href = '/login?redirect=/posts';
                return;
              }

              // 쿠키는 있지만 403이 발생한 경우 - 팔로잉이 없을 가능성
              console.log('쿠키는 존재하지만 403 에러 발생 - 팔로잉 확인 중...');
              setShowEmptyFollowing(true);
              await loadSuggestedUsers();
              setPosts([]);
            } else if (err.response?.data?.message?.includes('팔로우')) {
              // 팔로잉이 없는 경우
              setShowEmptyFollowing(true);
              await loadSuggestedUsers();
              setPosts([]);
            } else {
              throw err;
            }
          }
        }
      }
    } catch (err: any) {
      console.error('Failed to load posts:', err);
      setError(err.response?.data?.message || '게시글을 불러오는데 실패했습니다.');
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    loadPosts(page, sortBy, selectedCategory);
  }, [page, sortBy, selectedCategory]);

  const loadCategories = async () => {
    try {
      const data = await categoryService.getCategories();
      setCategories(data || []);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const loadSuggestedUsers = async () => {
    try {
      const users = await userService.getSuggestedUsers(0, 10);
      console.log('Current user ID:', user?.id);
      console.log('Suggested users:', users);
      // 현재 사용자 제외
      const filtered = users.filter(u => u.id !== user?.id);
      console.log('Filtered users:', filtered);
      setSuggestedUsers(filtered);
    } catch (err) {
      console.error('Failed to load suggested users:', err);
    }
  };

  const handleFollow = async (userId: number) => {
    try {
      console.log('Attempting to follow user:', userId);
      console.log('Current logged-in user:', user?.id);

      if (userId === user?.id) {
        alert('자기 자신은 팔로우할 수 없습니다.');
        return;
      }

      await followService.follow(userId);
      setFollowingUsers(prev => new Set(prev).add(userId));

      // 팔로우 성공 시 사용자에게 피드백
      alert('팔로우했습니다! 팔로잉 탭에서 해당 사용자의 게시글을 확인할 수 있습니다.');

      // 팔로우 성공 후 자동으로 팔로잉 탭으로 전환
      if (sortBy === 'following') {
        // 이미 팔로잉 탭이면 새로고침
        await loadPosts(0, 'following', null);
        setPage(0);
      }
    } catch (err: any) {
      console.error('Failed to follow user:', err);
      console.error('Error response:', err.response?.data);

      // 403 에러인 경우 로그인 상태 확인
      if (err.response?.status === 403) {
        const cookies = document.cookie;
        console.error('팔로우 시도 시 쿠키:', cookies);

        if (!cookies || cookies.trim() === '') {
          alert('로그인 세션이 만료되었습니다. 다시 로그인해주세요.');
          localStorage.removeItem('auth-storage');
          window.location.href = '/login?redirect=/posts';
          return;
        }
      }

      const errorMessage = err.response?.data?.message || '팔로우에 실패했습니다. 다시 시도해주세요.';
      alert(errorMessage);
    }
  };

  const handleSortChange = (newSort: 'latest' | 'popular' | 'trending' | 'following') => {
    setSortBy(newSort);
    setPage(0);
  };

  const handleCategoryChange = (categoryId: number | null) => {
    setSelectedCategory(categoryId);
    setPage(0);
  };

  const handlePreviousPage = () => {
    if (page > 0) {
      setPage(page - 1);
    }
  };

  const handleNextPage = () => {
    if (page < totalPages - 1) {
      setPage(page + 1);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">게시판</h1>
          <Link href="/posts/create">
            <Button className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white">
              글쓰기
            </Button>
          </Link>
        </div>

        {/* 카테고리 필터 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-4 mb-6">
          <div className="flex items-center gap-4">
            <label className="text-sm font-medium text-gray-700 dark:text-gray-300">카테고리:</label>
            <select
              value={selectedCategory || ''}
              onChange={(e) => handleCategoryChange(e.target.value ? Number(e.target.value) : null)}
              className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체 카테고리</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* 정렬 옵션 */}
        <div className="flex gap-2 mb-6">
          {isAuthenticated && (
            <button
              onClick={() => handleSortChange('following')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                sortBy === 'following'
                  ? 'bg-blue-600 dark:bg-blue-500 text-white'
                  : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
              }`}
            >
              팔로잉
            </button>
          )}
          <button
            onClick={() => handleSortChange('latest')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              sortBy === 'latest'
                ? 'bg-blue-600 dark:bg-blue-500 text-white'
                : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
            }`}
          >
            최신순
          </button>
          <button
            onClick={() => handleSortChange('popular')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              sortBy === 'popular'
                ? 'bg-blue-600 dark:bg-blue-500 text-white'
                : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
            }`}
          >
            인기순
          </button>
          <button
            onClick={() => handleSortChange('trending')}
            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
              sortBy === 'trending'
                ? 'bg-blue-600 dark:bg-blue-500 text-white'
                : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
            }`}
          >
            트렌딩
          </button>
        </div>

        {/* 로딩 상태 */}
        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        )}

        {/* 에러 상태 */}
        {error && !loading && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {/* 게시글 목록 */}
        {!loading && !error && posts && (
          <>
            {posts.length === 0 ? (
              showEmptyFollowing ? (
                <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-8">
                  <div className="text-center mb-6">
                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
                      팔로우한 사람이 없습니다
                    </h2>
                    <p className="text-gray-500 dark:text-gray-400">
                      다른 사용자를 팔로우하고 그들의 게시글을 확인해보세요
                    </p>
                  </div>

                  {suggestedUsers.length > 0 && (
                    <div className="space-y-4">
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                        추천 사용자
                      </h3>
                      <div className="grid gap-4">
                        {suggestedUsers.map((suggestedUser) => (
                          <div
                            key={suggestedUser.id}
                            className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors"
                          >
                            <div className="flex items-center gap-3">
                              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-bold">
                                {suggestedUser.nickname?.[0]?.toUpperCase() || 'U'}
                              </div>
                              <div>
                                <Link
                                  href={`/users/${suggestedUser.id}`}
                                  className="font-medium text-gray-900 dark:text-white hover:text-blue-600 dark:hover:text-blue-400"
                                >
                                  {suggestedUser.nickname || suggestedUser.email}
                                </Link>
                                {suggestedUser.profile?.bio && (
                                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                                    {suggestedUser.profile.bio}
                                  </p>
                                )}
                              </div>
                            </div>
                            <Button
                              onClick={() => handleFollow(suggestedUser.id)}
                              disabled={followingUsers.has(suggestedUser.id)}
                              className={
                                followingUsers.has(suggestedUser.id)
                                  ? 'bg-gray-400 dark:bg-gray-600 cursor-not-allowed'
                                  : 'bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white'
                              }
                            >
                              {followingUsers.has(suggestedUser.id) ? '팔로잉' : '팔로우'}
                            </Button>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-12">
                  <p className="text-gray-500 dark:text-gray-400 mb-4">아직 작성된 게시글이 없습니다.</p>
                  <Link href="/posts/create">
                    <Button className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white">
                      첫 게시글 작성하기
                    </Button>
                  </Link>
                </div>
              )
            ) : (
              <div className="space-y-4">
                {posts.map((post) => (
                  <PostCard key={post.id} post={post} />
                ))}
              </div>
            )}

            {/* 페이지네이션 */}
            {totalPages > 1 && posts.length > 0 && (
              <div className="flex items-center justify-center gap-2 mt-8">
                <button
                  onClick={handlePreviousPage}
                  disabled={page === 0}
                  className={`px-4 py-2 rounded-lg border ${
                    page === 0
                      ? 'bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 cursor-not-allowed border-gray-300 dark:border-gray-700'
                      : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
                  }`}
                >
                  이전
                </button>

                <div className="flex items-center gap-2">
                  {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                    let pageNum;
                    if (totalPages <= 5) {
                      pageNum = i;
                    } else if (page < 3) {
                      pageNum = i;
                    } else if (page > totalPages - 4) {
                      pageNum = totalPages - 5 + i;
                    } else {
                      pageNum = page - 2 + i;
                    }

                    return (
                      <button
                        key={pageNum}
                        onClick={() => setPage(pageNum)}
                        className={`px-4 py-2 rounded-lg ${
                          page === pageNum
                            ? 'bg-blue-600 dark:bg-blue-500 text-white'
                            : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
                        }`}
                      >
                        {pageNum + 1}
                      </button>
                    );
                  })}
                </div>

                <button
                  onClick={handleNextPage}
                  disabled={page >= totalPages - 1}
                  className={`px-4 py-2 rounded-lg border ${
                    page >= totalPages - 1
                      ? 'bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 cursor-not-allowed border-gray-300 dark:border-gray-700'
                      : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700'
                  }`}
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
