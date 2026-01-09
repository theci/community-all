'use client';

import { useState, useEffect } from 'react';
import { postService, categoryService } from '@/lib/services';
import { Post, Category } from '@ddd3/types';
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

  const loadPosts = async (currentPage: number, sort: string, categoryId?: number | null) => {
    try {
      setLoading(true);
      setError(null);

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
          response = await postService.getFollowingFeed(user.id, currentPage, 20);
          setPosts(response?.content || []);
          setTotalPages(response?.pageInfo?.totalPages || 0);
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
              <div className="text-center py-12">
                <p className="text-gray-500 dark:text-gray-400 mb-4">아직 작성된 게시글이 없습니다.</p>
                <Link href="/posts/create">
                  <Button className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white">
                    첫 게시글 작성하기
                  </Button>
                </Link>
              </div>
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
