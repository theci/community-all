'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';
import { postService } from '@/lib/services';
import type { Post } from '@/lib/types';

// 모바일 전용 미니 헤더
function MobileHeader() {
  const { isAuthenticated } = useAuth();

  return (
    <header className="sticky top-0 z-50 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 px-4 py-3">
      <div className="flex items-center justify-between">
        <Link href="/" className="flex items-center">
          <span className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            Community
          </span>
        </Link>
        <div className="flex items-center gap-3">
          {isAuthenticated && (
            <>
              {/* 알림 아이콘 */}
              <Link href="/notifications" className="relative">
                <svg className="w-6 h-6 text-gray-700 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                <span className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full text-xs text-white flex items-center justify-center">
                  3
                </span>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

// 카테고리 칩 (가로 스크롤)
function CategoryChips({ selected, onSelect }: { selected: string; onSelect: (category: string) => void }) {
  const categories = ['전체', '🔥 인기', '⚡ 트렌딩'];

  return (
    <div className="overflow-x-auto hide-scrollbar bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
      <div className="flex gap-2 px-4 py-3">
        {categories.map((category) => (
          <button
            key={category}
            onClick={() => onSelect(category)}
            className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
              selected === category
                ? 'bg-blue-600 text-white shadow-md'
                : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 active:bg-gray-200 dark:active:bg-gray-700'
            }`}
          >
            {category}
          </button>
        ))}
      </div>
    </div>
  );
}

// 모바일 전용 포스트 카드
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

// 로딩 스켈레톤
function PostCardSkeleton() {
  return (
    <div className="p-4 border-b border-gray-200 dark:border-gray-800 animate-pulse">
      <div className="flex items-center gap-2 mb-3">
        <div className="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-800"></div>
        <div className="flex-1">
          <div className="h-4 bg-gray-200 dark:bg-gray-800 rounded w-20 mb-1"></div>
          <div className="h-3 bg-gray-200 dark:bg-gray-800 rounded w-16"></div>
        </div>
      </div>
      <div className="h-5 bg-gray-200 dark:bg-gray-800 rounded w-3/4 mb-2"></div>
      <div className="h-4 bg-gray-200 dark:bg-gray-800 rounded w-full mb-1"></div>
      <div className="h-4 bg-gray-200 dark:bg-gray-800 rounded w-2/3 mb-3"></div>
      <div className="flex gap-4">
        <div className="h-4 bg-gray-200 dark:bg-gray-800 rounded w-12"></div>
        <div className="h-4 bg-gray-200 dark:bg-gray-800 rounded w-12"></div>
        <div className="h-4 bg-gray-200 dark:bg-gray-800 rounded w-12"></div>
      </div>
    </div>
  );
}

export default function MobileHome() {
  const { isAuthenticated } = useAuth();
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState('전체');

  useEffect(() => {
    loadPosts(selectedCategory);
  }, [selectedCategory]);

  const loadPosts = async (category: string) => {
    try {
      setLoading(true);
      let result: Post[] = [];

      if (category === '전체') {
        const [popular, trending] = await Promise.all([
          postService.getPopularPosts(10),
          postService.getTrendingPosts(10),
        ]);
        // 중복 제거하여 합치기
        const combined = [...(Array.isArray(popular) ? popular : []), ...(Array.isArray(trending) ? trending : [])];
        result = combined.filter((post, index, self) =>
          index === self.findIndex((p) => p.id === post.id)
        );
      } else if (category === '🔥 인기') {
        const popular = await postService.getPopularPosts(20);
        result = Array.isArray(popular) ? popular : [];
      } else if (category === '⚡ 트렌딩') {
        const trending = await postService.getTrendingPosts(20);
        result = Array.isArray(trending) ? trending : [];
      }

      setPosts(result);
    } catch (err) {
      console.error('Failed to load posts:', err);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-black">
      <MobileHeader />
      <CategoryChips selected={selectedCategory} onSelect={handleCategoryChange} />

      {/* 퀵 액션 (로그인 시) */}
      {isAuthenticated && (
        <div className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 p-4">
          <Link
            href="/posts/create"
            className="flex items-center gap-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-xl p-4 shadow-lg active:scale-95 transition-transform"
          >
            <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </div>
            <div>
              <div className="font-semibold">새 글 작성</div>
              <div className="text-xs text-white/80">지금 바로 생각을 공유하세요</div>
            </div>
          </Link>
        </div>
      )}

      {/* 피드 */}
      <div className="bg-white dark:bg-gray-900">
        {loading ? (
          <div>
            {[1, 2, 3, 4, 5].map((i) => (
              <PostCardSkeleton key={i} />
            ))}
          </div>
        ) : posts.length > 0 ? (
          <div>
            {posts.map((post) => (
              <MobilePostCard key={post.id} post={post} />
            ))}
          </div>
        ) : (
          <div className="p-12 text-center text-gray-500 dark:text-gray-400">
            <svg className="w-16 h-16 mx-auto mb-4 text-gray-300 dark:text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
            </svg>
            <p>게시글이 없습니다</p>
          </div>
        )}
      </div>

      {/* 하단 여백 (탭바 공간) */}
      <div className="h-4"></div>

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
