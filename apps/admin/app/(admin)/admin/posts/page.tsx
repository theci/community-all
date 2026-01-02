'use client';

import { useState, useEffect } from 'react';
import { adminService } from '@/lib/services';
import { PostManagement, UpdatePostStatusRequest, MarkAsNoticeRequest, BulkPostActionRequest, BulkActionType } from '@/lib/types';
import { PostStatus } from '@/lib/types/post';
import { Button, Input, Card } from '@/components/ui';
import { useAuth } from '@/lib/hooks';

export default function AdminPostsPage() {
  const { user } = useAuth();
  const [posts, setPosts] = useState<PostManagement[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // 필터
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [isNoticeFilter, setIsNoticeFilter] = useState<boolean | undefined>();

  // 선택된 게시글
  const [selectedPosts, setSelectedPosts] = useState<number[]>([]);

  // 모달
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [selectedPost, setSelectedPost] = useState<PostManagement | null>(null);

  useEffect(() => {
    loadPosts();
  }, [page, statusFilter, isNoticeFilter]);

  const loadPosts = async () => {
    try {
      setLoading(true);
      const response = await adminService.getPostList({
        status: statusFilter || undefined,
        keyword: searchKeyword || undefined,
        isNotice: isNoticeFilter,
        page,
        size: 20,
      });

      setPosts(response.content);
      setTotal(response.pageInfo.totalElements);
      setTotalPages(response.pageInfo.totalPages);
    } catch (error) {
      console.error('Failed to load posts:', error);
      alert('게시글 목록 로드 실패');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPage(0);
    loadPosts();
  };

  const handleStatusChange = async (postId: number, newStatus: PostStatus) => {
    if (!user?.id) return;

    try {
      const request: UpdatePostStatusRequest = {
        status: newStatus,
      };
      await adminService.updatePostStatus(postId, user.id, request);
      alert('게시글 상태가 변경되었습니다');
      loadPosts();
    } catch (error) {
      console.error('Failed to update post status:', error);
      alert('게시글 상태 변경 실패');
    }
  };

  const handleMarkAsNotice = async (postId: number, isNotice: boolean) => {
    try {
      const request: MarkAsNoticeRequest = { isNotice };
      await adminService.markPostAsNotice(postId, request);
      alert(isNotice ? '공지사항으로 지정되었습니다' : '공지사항이 해제되었습니다');
      loadPosts();
    } catch (error) {
      console.error('Failed to mark as notice:', error);
      alert('공지사항 처리 실패');
    }
  };

  const handleBulkAction = async (action: BulkActionType) => {
    if (!user?.id || selectedPosts.length === 0) {
      alert('게시글을 선택해주세요');
      return;
    }

    if (!confirm(`선택한 ${selectedPosts.length}개 게시글을 처리하시겠습니까?`)) {
      return;
    }

    try {
      const request: BulkPostActionRequest = {
        postIds: selectedPosts,
        action,
      };
      await adminService.bulkPostAction(user.id, request);
      alert('게시글이 일괄 처리되었습니다');
      setSelectedPosts([]);
      loadPosts();
    } catch (error) {
      console.error('Failed to bulk action:', error);
      alert('일괄 처리 실패');
    }
  };

  const toggleSelectPost = (postId: number) => {
    setSelectedPosts(prev =>
      prev.includes(postId) ? prev.filter(id => id !== postId) : [...prev, postId]
    );
  };

  const toggleSelectAll = () => {
    if (selectedPosts.length === posts.length) {
      setSelectedPosts([]);
    } else {
      setSelectedPosts(posts.map(p => p.postId));
    }
  };

  const getStatusBadgeClass = (status: PostStatus) => {
    switch (status) {
      case PostStatus.PUBLISHED:
        return 'bg-green-100 text-green-800';
      case PostStatus.DRAFT:
        return 'bg-yellow-100 text-yellow-800';
      case PostStatus.DELETED:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6">콘텐츠 관리</h1>

      {/* 필터 */}
      <Card className="mb-6">
        <div className="flex flex-wrap gap-4">
          <div className="flex-1 min-w-[200px]">
            <Input
              type="text"
              placeholder="제목으로 검색"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg"
          >
            <option value="">전체 상태</option>
            <option value="PUBLISHED">발행됨</option>
            <option value="DRAFT">임시저장</option>
            <option value="DELETED">삭제됨</option>
          </select>

          <select
            value={isNoticeFilter === undefined ? '' : String(isNoticeFilter)}
            onChange={(e) =>
              setIsNoticeFilter(e.target.value === '' ? undefined : e.target.value === 'true')
            }
            className="px-3 py-2 border border-gray-300 rounded-lg"
          >
            <option value="">전체</option>
            <option value="true">공지사항만</option>
            <option value="false">일반글만</option>
          </select>

          <Button onClick={handleSearch}>검색</Button>
        </div>
      </Card>

      {/* 일괄 처리 */}
      {selectedPosts.length > 0 && (
        <Card className="mb-4">
          <div className="flex items-center gap-4">
            <span className="font-medium">{selectedPosts.length}개 선택됨</span>
            <Button onClick={() => handleBulkAction(BulkActionType.DELETE)} className="bg-red-600">
              일괄 삭제
            </Button>
            <Button onClick={() => handleBulkAction(BulkActionType.RESTORE)}>일괄 복구</Button>
            <Button onClick={() => handleBulkAction(BulkActionType.MARK_AS_NOTICE)}>
              공지 지정
            </Button>
            <Button onClick={() => handleBulkAction(BulkActionType.UNMARK_AS_NOTICE)}>
              공지 해제
            </Button>
          </div>
        </Card>
      )}

      {/* 게시글 목록 */}
      <Card>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-gray-800">
              <tr>
                <th className="px-4 py-3">
                  <input
                    type="checkbox"
                    checked={posts.length > 0 && selectedPosts.length === posts.length}
                    onChange={toggleSelectAll}
                  />
                </th>
                <th className="px-4 py-3 text-left">ID</th>
                <th className="px-4 py-3 text-left">제목</th>
                <th className="px-4 py-3 text-left">작성자</th>
                <th className="px-4 py-3 text-left">카테고리</th>
                <th className="px-4 py-3 text-left">상태</th>
                <th className="px-4 py-3 text-center">공지</th>
                <th className="px-4 py-3 text-center">조회</th>
                <th className="px-4 py-3 text-center">좋아요</th>
                <th className="px-4 py-3 text-left">작성일</th>
                <th className="px-4 py-3 text-center">관리</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={11} className="text-center py-8">
                    로딩 중...
                  </td>
                </tr>
              ) : posts.length === 0 ? (
                <tr>
                  <td colSpan={11} className="text-center py-8 text-gray-500">
                    게시글이 없습니다
                  </td>
                </tr>
              ) : (
                posts.map((post) => (
                  <tr key={post.postId} className="border-t hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <input
                        type="checkbox"
                        checked={selectedPosts.includes(post.postId)}
                        onChange={() => toggleSelectPost(post.postId)}
                      />
                    </td>
                    <td className="px-4 py-3">{post.postId}</td>
                    <td className="px-4 py-3">
                      <div className="max-w-md">
                        <div className="font-medium truncate">{post.title}</div>
                        <div className="text-sm text-gray-500 truncate">{post.summary}</div>
                      </div>
                    </td>
                    <td className="px-4 py-3">{post.authorNickname}</td>
                    <td className="px-4 py-3">{post.categoryName}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`px-2 py-1 rounded text-xs font-medium ${getStatusBadgeClass(
                          post.status
                        )}`}
                      >
                        {post.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      {post.isNoticePost && (
                        <span className="text-red-600 font-bold">공지</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-center">{post.viewCount}</td>
                    <td className="px-4 py-3 text-center">{post.likeCount}</td>
                    <td className="px-4 py-3 text-sm">
                      {new Date(post.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        {post.status === PostStatus.PUBLISHED && (
                          <button
                            onClick={() => handleStatusChange(post.postId, PostStatus.DELETED)}
                            className="text-red-600 text-sm hover:underline"
                          >
                            삭제
                          </button>
                        )}
                        {post.status === PostStatus.DELETED && (
                          <button
                            onClick={() => handleStatusChange(post.postId, PostStatus.PUBLISHED)}
                            className="text-green-600 text-sm hover:underline"
                          >
                            복구
                          </button>
                        )}
                        <button
                          onClick={() => handleMarkAsNotice(post.postId, !post.isNoticePost)}
                          className="text-blue-600 text-sm hover:underline"
                        >
                          {post.isNoticePost ? '공지해제' : '공지지정'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div className="flex justify-center gap-2 mt-4 pt-4 border-t">
            <Button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
              이전
            </Button>
            <span className="px-4 py-2">
              {page + 1} / {totalPages}
            </span>
            <Button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
            >
              다음
            </Button>
          </div>
        )}
      </Card>
    </div>
  );
}
