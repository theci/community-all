'use client';

import { useState, useEffect } from 'react';
import { adminService } from '@/lib/services';
import type { UserManagement, UserDetail } from '@/lib/types';
import { UserStatus, UserRole } from '@/lib/types/user';
import { Button, Modal } from '@/components/ui';

export default function UserManagementPage() {
  const [users, setUsers] = useState<UserManagement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // 필터
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<UserStatus | ''>('');
  const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');

  // 사용자 상세 모달
  const [selectedUser, setSelectedUser] = useState<UserDetail | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  // 상태 변경 모달
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [newStatus, setNewStatus] = useState<UserStatus>(UserStatus.ACTIVE);
  const [statusReason, setStatusReason] = useState('');

  // 역할 변경 모달
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [newRole, setNewRole] = useState<UserRole>(UserRole.USER);

  // 포인트 조정 모달
  const [showPointModal, setShowPointModal] = useState(false);
  const [pointAmount, setPointAmount] = useState<number>(0);
  const [pointReason, setPointReason] = useState('');

  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    loadUsers();
  }, [page, statusFilter, roleFilter]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await adminService.getUserList({
        keyword: keyword || undefined,
        status: statusFilter || undefined,
        role: roleFilter || undefined,
        page,
        size: 20,
      });

      setUsers(response.content);
      setTotalPages(response.pageInfo.totalPages);
    } catch (err: any) {
      console.error('Failed to load users:', err);
      setError(err.response?.data?.message || '사용자 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPage(0);
    loadUsers();
  };

  const handleViewDetail = async (userId: number) => {
    try {
      const detail = await adminService.getUserDetail(userId);
      setSelectedUser(detail);
      setShowDetailModal(true);
    } catch (err: any) {
      console.error('Failed to load user detail:', err);
      alert(err.response?.data?.message || '사용자 상세를 불러오는데 실패했습니다.');
    }
  };

  const handleUpdateStatus = async () => {
    if (!selectedUser) return;

    try {
      setIsProcessing(true);
      await adminService.updateUserStatus(selectedUser.userId, {
        status: newStatus,
        reason: statusReason || undefined,
      });

      alert('사용자 상태가 변경되었습니다.');
      setShowStatusModal(false);
      setStatusReason('');
      loadUsers();
      if (selectedUser) {
        const updated = await adminService.getUserDetail(selectedUser.userId);
        setSelectedUser(updated);
      }
    } catch (err: any) {
      console.error('Failed to update user status:', err);
      alert(err.response?.data?.message || '사용자 상태 변경에 실패했습니다.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleUpdateRole = async () => {
    if (!selectedUser) return;

    try {
      setIsProcessing(true);
      await adminService.updateUserRole(selectedUser.userId, {
        role: newRole,
      });

      alert('사용자 역할이 변경되었습니다.');
      setShowRoleModal(false);
      loadUsers();
      if (selectedUser) {
        const updated = await adminService.getUserDetail(selectedUser.userId);
        setSelectedUser(updated);
      }
    } catch (err: any) {
      console.error('Failed to update user role:', err);
      alert(err.response?.data?.message || '사용자 역할 변경에 실패했습니다.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleAdjustPoint = async () => {
    if (!selectedUser) return;

    if (!pointReason.trim()) {
      alert('사유를 입력해주세요.');
      return;
    }

    try {
      setIsProcessing(true);
      await adminService.adjustUserPoint(selectedUser.userId, {
        points: pointAmount,
        reason: pointReason,
      });

      alert('포인트가 조정되었습니다.');
      setShowPointModal(false);
      setPointAmount(0);
      setPointReason('');
      if (selectedUser) {
        const updated = await adminService.getUserDetail(selectedUser.userId);
        setSelectedUser(updated);
      }
    } catch (err: any) {
      console.error('Failed to adjust user point:', err);
      alert(err.response?.data?.message || '포인트 조정에 실패했습니다.');
    } finally {
      setIsProcessing(false);
    }
  };

  const getStatusBadge = (status: UserStatus) => {
    switch (status) {
      case UserStatus.ACTIVE:
        return (
          <span className="px-2 py-1 text-xs font-medium bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 rounded">
            활성
          </span>
        );
      case UserStatus.INACTIVE:
        return (
          <span className="px-2 py-1 text-xs font-medium bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-400 rounded">
            비활성
          </span>
        );
      case UserStatus.BLOCKED:
        return (
          <span className="px-2 py-1 text-xs font-medium bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded">
            차단됨
          </span>
        );
    }
  };

  const getRoleBadge = (role: UserRole) => {
    const roleColors: Record<UserRole, string> = {
      [UserRole.USER]: 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300',
      [UserRole.POWER_USER]: 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400',
      [UserRole.MODERATOR]: 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400',
      [UserRole.ADMIN]: 'bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400',
      [UserRole.SUPER_ADMIN]: 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400',
    };

    const roleLabels: Record<UserRole, string> = {
      [UserRole.USER]: '일반',
      [UserRole.POWER_USER]: '파워',
      [UserRole.MODERATOR]: '모더',
      [UserRole.ADMIN]: '관리자',
      [UserRole.SUPER_ADMIN]: '최고관리자',
    };

    return (
      <span className={`px-2 py-1 text-xs font-medium rounded ${roleColors[role]}`}>
        {roleLabels[role]}
      </span>
    );
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="space-y-6">
      {/* 페이지 헤더 */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">사용자 관리</h1>
        <p className="mt-2 text-gray-600 dark:text-gray-400">
          사용자 정보를 조회하고 관리하세요
        </p>
      </div>

      {/* 검색 및 필터 */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="md:col-span-2">
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="이메일 또는 닉네임 검색..."
              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as UserStatus | '');
              setPage(0);
            }}
            className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">전체 상태</option>
            <option value={UserStatus.ACTIVE}>활성</option>
            <option value={UserStatus.INACTIVE}>비활성</option>
            <option value={UserStatus.BLOCKED}>차단됨</option>
          </select>
          <select
            value={roleFilter}
            onChange={(e) => {
              setRoleFilter(e.target.value as UserRole | '');
              setPage(0);
            }}
            className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">전체 역할</option>
            <option value={UserRole.USER}>일반 사용자</option>
            <option value={UserRole.POWER_USER}>파워 유저</option>
            <option value={UserRole.MODERATOR}>모더레이터</option>
            <option value={UserRole.ADMIN}>관리자</option>
            <option value={UserRole.SUPER_ADMIN}>최고 관리자</option>
          </select>
        </div>
        <div className="mt-3">
          <Button
            onClick={handleSearch}
            className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white"
          >
            검색
          </Button>
        </div>
      </div>

      {/* 로딩 상태 */}
      {loading && (
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
        </div>
      )}

      {/* 에러 상태 */}
      {error && !loading && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {/* 사용자 목록 */}
      {!loading && !error && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-900">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    이메일
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    닉네임
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    상태
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    역할
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    레벨/포인트
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    게시글/댓글
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    가입일
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    액션
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {users.length === 0 ? (
                  <tr>
                    <td colSpan={9} className="px-6 py-12 text-center text-gray-500 dark:text-gray-400">
                      사용자가 없습니다.
                    </td>
                  </tr>
                ) : (
                  users.map((user) => (
                    <tr key={user.userId} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {user.userId}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {user.email}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {user.nickname}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(user.status)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getRoleBadge(user.role)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        Lv.{user.currentLevel} / {user.currentPoints}P
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {user.totalPosts} / {user.totalComments}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                        {formatDate(user.createdAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <Button
                          onClick={() => handleViewDetail(user.userId)}
                          className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white text-xs px-3 py-1"
                        >
                          상세보기
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 px-6 py-4 border-t border-gray-200 dark:border-gray-700">
              <button
                onClick={() => setPage(page - 1)}
                disabled={page === 0}
                className={`px-4 py-2 rounded-lg ${
                  page === 0
                    ? 'bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 cursor-not-allowed'
                    : 'bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 border border-gray-300 dark:border-gray-600'
                }`}
              >
                이전
              </button>
              <span className="text-sm text-gray-600 dark:text-gray-400">
                {page + 1} / {totalPages}
              </span>
              <button
                onClick={() => setPage(page + 1)}
                disabled={page >= totalPages - 1}
                className={`px-4 py-2 rounded-lg ${
                  page >= totalPages - 1
                    ? 'bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 cursor-not-allowed'
                    : 'bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 border border-gray-300 dark:border-gray-600'
                }`}
              >
                다음
              </button>
            </div>
          )}
        </div>
      )}

      {/* 사용자 상세 모달 */}
      {selectedUser && (
        <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedUser(null);
          }}
          title="사용자 상세 정보"
        >
          <div className="space-y-4">
            {/* 기본 정보 */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">이메일</label>
                <p className="mt-1 text-gray-900 dark:text-white">{selectedUser.email}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">닉네임</label>
                <p className="mt-1 text-gray-900 dark:text-white">{selectedUser.nickname}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">상태</label>
                <div className="mt-1">{getStatusBadge(selectedUser.status)}</div>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">역할</label>
                <div className="mt-1">{getRoleBadge(selectedUser.role)}</div>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">레벨</label>
                <p className="mt-1 text-gray-900 dark:text-white">Lv. {selectedUser.currentLevel}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">포인트</label>
                <p className="mt-1 text-gray-900 dark:text-white">{selectedUser.currentPoints}P</p>
              </div>
            </div>

            {/* 활동 통계 */}
            <div className="border-t border-gray-200 dark:border-gray-700 pt-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">활동 통계</h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">게시글</label>
                  <p className="mt-1 text-gray-900 dark:text-white">{selectedUser.totalPosts}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">댓글</label>
                  <p className="mt-1 text-gray-900 dark:text-white">{selectedUser.totalComments}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">좋아요</label>
                  <p className="mt-1 text-gray-900 dark:text-white">{selectedUser.totalLikes}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-700 dark:text-gray-300">스크랩</label>
                  <p className="mt-1 text-gray-900 dark:text-white">{selectedUser.totalScraps}</p>
                </div>
              </div>
            </div>

            {/* 관리 액션 */}
            <div className="border-t border-gray-200 dark:border-gray-700 pt-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">관리 액션</h3>
              <div className="flex flex-wrap gap-2">
                <Button
                  onClick={() => {
                    setNewStatus(selectedUser.status);
                    setShowStatusModal(true);
                  }}
                  className="bg-yellow-600 dark:bg-yellow-500 hover:bg-yellow-700 dark:hover:bg-yellow-600 text-white"
                >
                  상태 변경
                </Button>
                <Button
                  onClick={() => {
                    setNewRole(selectedUser.role);
                    setShowRoleModal(true);
                  }}
                  className="bg-purple-600 dark:bg-purple-500 hover:bg-purple-700 dark:hover:bg-purple-600 text-white"
                >
                  역할 변경
                </Button>
                <Button
                  onClick={() => {
                    setPointAmount(0);
                    setPointReason('');
                    setShowPointModal(true);
                  }}
                  className="bg-green-600 dark:bg-green-500 hover:bg-green-700 dark:hover:bg-green-600 text-white"
                >
                  포인트 조정
                </Button>
              </div>
            </div>

            {/* 제재 이력 */}
            {selectedUser.sanctions.length > 0 && (
              <div className="border-t border-gray-200 dark:border-gray-700 pt-4">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">제재 이력</h3>
                <div className="space-y-2">
                  {selectedUser.sanctions.map((sanction) => (
                    <div
                      key={sanction.sanctionId}
                      className="p-3 bg-gray-50 dark:bg-gray-900 rounded border border-gray-200 dark:border-gray-700"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="font-medium text-gray-900 dark:text-white">{sanction.type}</p>
                          <p className="text-sm text-gray-600 dark:text-gray-400">{sanction.reason}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-500 mt-1">
                            {formatDate(sanction.startDate)} ~ {formatDate(sanction.endDate)}
                          </p>
                        </div>
                        <span
                          className={`px-2 py-1 text-xs rounded ${
                            sanction.status === 'ACTIVE'
                              ? 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400'
                              : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-400'
                          }`}
                        >
                          {sanction.status}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </Modal>
      )}

      {/* 상태 변경 모달 */}
      <Modal
        isOpen={showStatusModal}
        onClose={() => setShowStatusModal(false)}
        title="사용자 상태 변경"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              새로운 상태
            </label>
            <select
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value as UserStatus)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"
            >
              <option value={UserStatus.ACTIVE}>활성</option>
              <option value={UserStatus.INACTIVE}>비활성</option>
              <option value={UserStatus.BLOCKED}>차단됨</option>
            </select>
          </div>
          {newStatus === UserStatus.BLOCKED && (
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                차단 사유
              </label>
              <textarea
                value={statusReason}
                onChange={(e) => setStatusReason(e.target.value)}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"
                placeholder="차단 사유를 입력하세요"
              />
            </div>
          )}
          <div className="flex justify-end gap-3 pt-4">
            <Button
              onClick={() => setShowStatusModal(false)}
              className="bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300"
            >
              취소
            </Button>
            <Button
              onClick={handleUpdateStatus}
              className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white"
              disabled={isProcessing}
              isLoading={isProcessing}
            >
              변경
            </Button>
          </div>
        </div>
      </Modal>

      {/* 역할 변경 모달 */}
      <Modal
        isOpen={showRoleModal}
        onClose={() => setShowRoleModal(false)}
        title="사용자 역할 변경"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              새로운 역할
            </label>
            <select
              value={newRole}
              onChange={(e) => setNewRole(e.target.value as UserRole)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"
            >
              <option value={UserRole.USER}>일반 사용자</option>
              <option value={UserRole.POWER_USER}>파워 유저</option>
              <option value={UserRole.MODERATOR}>모더레이터</option>
              <option value={UserRole.ADMIN}>관리자</option>
              <option value={UserRole.SUPER_ADMIN}>최고 관리자</option>
            </select>
          </div>
          <div className="flex justify-end gap-3 pt-4">
            <Button
              onClick={() => setShowRoleModal(false)}
              className="bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300"
            >
              취소
            </Button>
            <Button
              onClick={handleUpdateRole}
              className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white"
              disabled={isProcessing}
              isLoading={isProcessing}
            >
              변경
            </Button>
          </div>
        </div>
      </Modal>

      {/* 포인트 조정 모달 */}
      <Modal
        isOpen={showPointModal}
        onClose={() => setShowPointModal(false)}
        title="포인트 조정"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              조정할 포인트 (+ 증가, - 감소)
            </label>
            <input
              type="number"
              value={pointAmount}
              onChange={(e) => setPointAmount(parseInt(e.target.value) || 0)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"
              placeholder="예: 100 또는 -50"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              사유
            </label>
            <textarea
              value={pointReason}
              onChange={(e) => setPointReason(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100"
              placeholder="조정 사유를 입력하세요"
            />
          </div>
          <div className="flex justify-end gap-3 pt-4">
            <Button
              onClick={() => setShowPointModal(false)}
              className="bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300"
            >
              취소
            </Button>
            <Button
              onClick={handleAdjustPoint}
              className="bg-blue-600 dark:bg-blue-500 hover:bg-blue-700 dark:hover:bg-blue-600 text-white"
              disabled={isProcessing}
              isLoading={isProcessing}
            >
              조정
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
