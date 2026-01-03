'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { postService, categoryService } from '@/lib/services';
import { Post, Category } from '@/lib/types';

// 모바일 포스트 카드
function MobilePostCard({ post }: { post: Post }) {
  return (
    <Link href={`/posts/${post.id}`} className="block bg-white dark:bg-gray-900 active:bg-gray-50 dark:active:bg-gray-800 transition-colors">
      <div className="p-4 border-b border-gray-200 dark:border-gray-800">
        {/* 작성자 정보 */}
        <div className="flex items-center gap-2 mb-3">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-600 flex items-center justify-center text-white text-sm font-semibold">
            {post.author?.nickname?.charAt(0).toUpperCase() || 'U'}
          </div>
          <div className="flex-1">
            <div className="text-sm font-medium text-gray-900 dark:text-white">
              {post.author?.nickname || '익명'}
            </div>
            <div className="text-xs text-gray-500 dark:text-gray-400">
              {new Date(post.createdAt).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
            </div>
          </div>
          {post.category && (
            <span className="px-2 py-1 text-xs rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
              {post.category.name}
            </span>
          )}
        </div>

        {/* 제목 & 내용 */}
        <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-2 line-clamp-2">
          {post.title}
        </h3>
        {post.summary && (
          <p className="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mb-3">
            {post.summary}
          </p>
        )}

        {/* 상호작용 버튼 */}
        <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
          <div className="flex items-center gap-1">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            <span>{post.likeCount}</span>
          </div>
          <div className="flex items-center gap-1">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            <span>{post.commentCount}</span>
          </div>
          <div className="flex items-center gap-1">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            <span>{post.viewCount}</span>
          </div>
        </div>
      </div>
    </Link>
  );
}

export default function MobilePostsPage() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [sortBy, setSortBy] = useState<'latest' | 'popular' | 'trending'>('latest');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [showSearch, setShowSearch] = useState(false);

  const loadPosts = async (currentPage: number, sort: string, keyword?: string, categoryId?: number | null, append = false) => {
    try {
      setLoading(true);
      setError(null);

      let response;

      if (keyword || categoryId) {
        if (keyword) {
          response = await postService.searchPosts({ keyword, categoryId: categoryId || undefined }, currentPage, 20);
        } else if (categoryId) {
          response = await postService.getPosts(currentPage, 20, 'createdAt,desc');
        }
        const newPosts = response?.content || [];
        setPosts(append ? [...posts, ...newPosts] : newPosts);
        setHasMore(newPosts.length === 20);
      } else {
        if (sort === 'latest') {
          response = await postService.getPosts(currentPage, 20, 'createdAt,desc');
          const newPosts = response?.content || [];
          setPosts(append ? [...posts, ...newPosts] : newPosts);
          setHasMore(newPosts.length === 20);
        } else if (sort === 'popular') {
          const popularPosts = await postService.getPopularPosts(20);
          setPosts(popularPosts || []);
          setHasMore(false);
        } else if (sort === 'trending') {
          const trendingPosts = await postService.getTrendingPosts(20);
          setPosts(trendingPosts || []);
          setHasMore(false);
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
    loadPosts(0, sortBy, searchKeyword, selectedCategory);
  }, [sortBy, searchKeyword, selectedCategory]);

  const loadCategories = async () => {
    try {
      const data = await categoryService.getCategories();
      setCategories(data || []);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const handleSortChange = (newSort: 'latest' | 'popular' | 'trending') => {
    setSortBy(newSort);
    setPage(0);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearchKeyword(searchInput);
    setPage(0);
    setShowSearch(false);
  };

  const handleCategoryChange = (categoryId: number | null) => {
    setSelectedCategory(categoryId);
    setPage(0);
  };

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    loadPosts(nextPage, sortBy, searchKeyword, selectedCategory, true);
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-black">
      {/* 상단 헤더 */}
      <div className="sticky top-0 z-50 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
        <div className="flex items-center justify-between px-4 py-3">
          <h1 className="text-lg font-bold text-gray-900 dark:text-white">게시판</h1>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowSearch(!showSearch)}
              className="p-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </button>
            <Link
              href="/posts/create"
              className="p-2 bg-blue-600 text-white rounded-lg active:bg-blue-700 transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </Link>
          </div>
        </div>

        {/* 검색 바 (토글) */}
        {showSearch && (
          <div className="px-4 pb-3">
            <form onSubmit={handleSearch} className="flex gap-2">
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="게시글 검색..."
                className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
                autoFocus
              />
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg active:bg-blue-700 transition-colors font-medium"
              >
                검색
              </button>
            </form>
          </div>
        )}
      </div>

      {/* 정렬 & 카테고리 */}
      <div className="sticky top-[57px] z-40 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
        {/* 정렬 버튼 */}
        <div className="flex gap-2 px-4 py-3 border-b border-gray-200 dark:border-gray-800">
          <button
            onClick={() => handleSortChange('latest')}
            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
              sortBy === 'latest'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300'
            }`}
          >
            최신순
          </button>
          <button
            onClick={() => handleSortChange('popular')}
            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
              sortBy === 'popular'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300'
            }`}
          >
            🔥 인기순
          </button>
          <button
            onClick={() => handleSortChange('trending')}
            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
              sortBy === 'trending'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300'
            }`}
          >
            ⚡ 트렌딩
          </button>
        </div>

        {/* 카테고리 칩 */}
        <div className="overflow-x-auto hide-scrollbar">
          <div className="flex gap-2 px-4 py-3">
            <button
              onClick={() => handleCategoryChange(null)}
              className={`px-3 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
                selectedCategory === null
                  ? 'bg-purple-600 text-white shadow-sm'
                  : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300'
              }`}
            >
              전체
            </button>
            {categories.map((category) => (
              <button
                key={category.id}
                onClick={() => handleCategoryChange(category.id)}
                className={`px-3 py-1.5 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
                  selectedCategory === category.id
                    ? 'bg-purple-600 text-white shadow-sm'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300'
                }`}
              >
                {category.name}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* 게시글 목록 */}
      <div className="bg-white dark:bg-gray-900">
        {loading && posts.length === 0 ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
          </div>
        ) : error && posts.length === 0 ? (
          <div className="p-4 m-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 rounded-lg">
            {error}
          </div>
        ) : posts.length === 0 ? (
          <div className="p-12 text-center text-gray-500 dark:text-gray-400">
            <svg className="w-16 h-16 mx-auto mb-4 text-gray-300 dark:text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
            </svg>
            <p className="mb-4">게시글이 없습니다</p>
            <Link
              href="/posts/create"
              className="inline-block px-6 py-2 bg-blue-600 text-white rounded-lg active:bg-blue-700 transition-colors font-medium"
            >
              첫 게시글 작성하기
            </Link>
          </div>
        ) : (
          <>
            {posts.map((post) => (
              <MobilePostCard key={post.id} post={post} />
            ))}

            {/* 더보기 버튼 */}
            {hasMore && !loading && (
              <div className="p-4">
                <button
                  onClick={handleLoadMore}
                  className="w-full py-3 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-lg font-medium active:bg-gray-200 dark:active:bg-gray-700 transition-colors"
                >
                  더보기
                </button>
              </div>
            )}

            {/* 로딩 중 (더보기) */}
            {loading && posts.length > 0 && (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            )}
          </>
        )}
      </div>

      <style jsx>{`
        .hide-scrollbar::-webkit-scrollbar {
          display: none;
        }
        .hide-scrollbar {
          -ms-overflow-style: none;
          scrollbar-width: none;
        }
      `}</style>
    </div>
  );
}
