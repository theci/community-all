'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';
import rehypeSanitize from 'rehype-sanitize';
import { postService } from '@/lib/services';
import { Post } from '@/lib/types';
import { useAuth } from '@/lib/hooks';
import { Button, Modal } from '@/components/ui';
import { CommentList } from '@/components/features/comment';

export default function PostDetailPage() {
  const router = useRouter();
  const params = useParams();
  const postId = Number(params.id);
  const { user, isAuthenticated } = useAuth();

  const [post, setPost] = useState<Post | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [scraped, setScraped] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    loadPost();
  }, [postId]);

  useEffect(() => {
    if (isAuthenticated && user?.id) {
      loadLikeStatus();
      loadScrapStatus();
    }
  }, [postId, isAuthenticated, user]);

  const loadPost = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await postService.getPost(postId);
      setPost(data);
      setLikeCount(data.likeCount);
    } catch (err: any) {
      console.error('Failed to load post:', err);
      setError(err.response?.data?.message || '게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const loadLikeStatus = async () => {
    if (!user?.id) return;
    try {
      const status = await postService.getLikeStatus(postId, user.id);
      setLiked(status.liked);
    } catch (err) {
      console.error('Failed to load like status:', err);
    }
  };

  const loadScrapStatus = async () => {
    if (!user?.id) return;
    try {
      const status = await postService.getScrapStatus(postId, user.id);
      setScraped(status.scraped);
    } catch (err) {
      console.error('Failed to load scrap status:', err);
      // 에러 시 기본값은 false
      setScraped(false);
    }
  };

  const handleLike = async () => {
    if (!isAuthenticated || !user?.id) {
      alert('로그인이 필요합니다.');
      router.push(`/login?redirect=/posts/${postId}`);
      return;
    }

    try {
      const result = await postService.toggleLike(postId, user.id);
      setLiked(result.liked);
      setLikeCount(result.likeCount);
    } catch (err: any) {
      console.error('Failed to toggle like:', err);
      alert(err.response?.data?.message || '좋아요 처리에 실패했습니다.');
    }
  };

  const handleScrap = async () => {
    if (!isAuthenticated || !user?.id) {
      alert('로그인이 필요합니다.');
      router.push(`/login?redirect=/posts/${postId}`);
      return;
    }

    try {
      // 현재 상태를 기준으로 적절한 API 호출
      const result = await postService.toggleScrap(postId, user.id, scraped);
      setScraped(result.scraped);

      // 성공 후 백엔드 상태와 동기화
      await loadScrapStatus();

      alert(result.scraped ? '스크랩했습니다.' : '스크랩을 취소했습니다.');
    } catch (err: any) {
      console.error('Failed to toggle scrap:', err);

      // "이미 스크랩한 게시글입니다" 에러 처리
      if (err.response?.data?.errorCode === 'INVALID_STATE') {
        // 백엔드와 프론트엔드 상태가 불일치 - 백엔드 상태를 신뢰
        await loadScrapStatus();
        alert('상태를 동기화했습니다. 다시 시도해주세요.');
      } else {
        alert(err.response?.data?.message || '스크랩 처리에 실패했습니다.');
        // 에러 발생 시에도 상태 다시 로드하여 동기화
        await loadScrapStatus();
      }
    }
  };

  const handleDelete = async () => {
    if (!user?.id) {
      alert('사용자 정보를 불러올 수 없습니다. 다시 로그인해주세요.');
      return;
    }

    try {
      setIsDeleting(true);
      await postService.deletePost(postId, user.id);
      alert('게시글이 삭제되었습니다.');
      router.push('/posts');
    } catch (err: any) {
      console.error('Failed to delete post:', err);
      alert(err.response?.data?.message || '게시글 삭제에 실패했습니다.');
    } finally {
      setIsDeleting(false);
      setShowDeleteModal(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">게시글을 불러올 수 없습니다</h2>
          <p className="text-gray-600 dark:text-gray-400 mb-6">{error}</p>
          <Link href="/posts">
            <Button className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white">
              목록으로 돌아가기
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  const isAuthor = user?.id === post.author.id;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* 상단 네비게이션 */}
        <div className="mb-6">
          <Link href="/posts" className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-500 font-medium">
            ← 목록으로
          </Link>
        </div>

        {/* 게시글 헤더 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-8 mb-4 border border-gray-200 dark:border-gray-700">
          {/* 카테고리 & 공지 */}
          <div className="flex items-center gap-2 mb-4">
            {post.category && (
              <span className="px-3 py-1 text-sm font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 rounded">
                {post.category.name}
              </span>
            )}
            {post.isNotice && (
              <span className="px-3 py-1 text-sm font-medium bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded">
                공지
              </span>
            )}
          </div>

          {/* 제목 */}
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">{post.title}</h1>

          {/* 메타 정보 */}
          <div className="flex items-center justify-between pb-6 border-b border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-4">
              <span className="font-medium text-gray-900 dark:text-white">
                {post.author.nickname || post.author.username}
              </span>
              <span className="text-gray-500 dark:text-gray-400 text-sm">
                {formatDate(post.createdAt)}
              </span>
            </div>

            <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                {post.viewCount}
              </span>
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                {post.commentCount}
              </span>
            </div>
          </div>

          {/* 태그 */}
          {post.tags && post.tags.length > 0 && (
            <div className="flex flex-wrap gap-2 pt-6 pb-6 border-b border-gray-200 dark:border-gray-700">
              {post.tags.map((tag) => (
                <span
                  key={tag.id}
                  className="px-3 py-1 text-sm text-gray-600 dark:text-gray-400 bg-gray-100 dark:bg-gray-700 rounded hover:bg-gray-200 dark:hover:bg-gray-600 cursor-pointer"
                >
                  #{tag.name}
                </span>
              ))}
            </div>
          )}

          {/* 본문 */}
          <div className="prose prose-lg dark:prose-invert max-w-none mt-8 mb-8 text-gray-800 dark:text-gray-300 leading-relaxed">
            <ReactMarkdown
              remarkPlugins={[remarkGfm]}
              rehypePlugins={[rehypeRaw, rehypeSanitize]}
            >
              {post.content}
            </ReactMarkdown>
          </div>

          {/* 액션 버튼 */}
          <div className="flex items-center justify-between pt-6 border-t border-gray-200 dark:border-gray-700">
            <div className="flex items-center gap-3">
              <button
                onClick={handleLike}
                className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-colors ${
                  liked
                    ? 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 hover:bg-red-200 dark:hover:bg-red-900/50'
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                }`}
              >
                <svg className="w-5 h-5" fill={liked ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
                좋아요 {likeCount}
              </button>

              <button
                onClick={handleScrap}
                className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-colors ${
                  scraped
                    ? 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400 hover:bg-yellow-200 dark:hover:bg-yellow-900/50'
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                }`}
              >
                <svg className="w-5 h-5" fill={scraped ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                </svg>
                스크랩
              </button>
            </div>

            {/* 작성자 전용 버튼 */}
            {isAuthor && (
              <div className="flex items-center gap-2">
                <Link href={`/posts/${postId}/edit`}>
                  <Button className="bg-gray-600 dark:bg-gray-700 hover:bg-gray-700 dark:hover:bg-gray-600 text-white">
                    수정
                  </Button>
                </Link>
                <Button
                  onClick={() => setShowDeleteModal(true)}
                  className="bg-red-600 dark:bg-red-700 hover:bg-red-700 dark:hover:bg-red-600 text-white"
                >
                  삭제
                </Button>
              </div>
            )}
          </div>
        </div>

        {/* 댓글 영역 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-8 border border-gray-200 dark:border-gray-700">
          <CommentList postId={postId} />
        </div>

        {/* 삭제 확인 모달 */}
        <Modal
          isOpen={showDeleteModal}
          onClose={() => setShowDeleteModal(false)}
          title="게시글 삭제"
        >
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            정말로 이 게시글을 삭제하시겠습니까?<br />
            삭제된 게시글은 복구할 수 없습니다.
          </p>
          <div className="flex justify-end gap-3">
            <Button
              onClick={() => setShowDeleteModal(false)}
              className="bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300"
              disabled={isDeleting}
            >
              취소
            </Button>
            <Button
              onClick={handleDelete}
              className="bg-red-600 dark:bg-red-700 hover:bg-red-700 dark:hover:bg-red-600 text-white"
              disabled={isDeleting}
              isLoading={isDeleting}
            >
              {isDeleting ? '삭제 중...' : '삭제'}
            </Button>
          </div>
        </Modal>
      </div>
    </div>
  );
}
