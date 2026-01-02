'use client';

import { useState, useEffect } from 'react';
import { adminService } from '@/lib/services';
import { useAuth } from '@/lib/hooks';
import type { Report, ReportDetail } from '@/lib/types';
import { Button, Modal } from '@/components/ui';

export default function ReportsManagementPage() {
  const { user } = useAuth();
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filter, setFilter] = useState<'PENDING' | 'APPROVED' | 'REJECTED' | 'ALL'>('PENDING');

  const [selectedReport, setSelectedReport] = useState<ReportDetail | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [processingNote, setProcessingNote] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  useEffect(() => {
    loadReports();
  }, [page, filter]);

  const loadReports = async () => {
    try {
      setLoading(true);
      setError(null);

      let response;
      if (filter === 'PENDING') {
        response = await adminService.getPendingReports(page, 20);
      } else if (filter === 'ALL') {
        response = await adminService.getAllReports(page, 20);
      } else {
        response = await adminService.getAllReports(page, 20, filter);
      }

      setReports(response.content);
      setTotalPages(response.pageInfo.totalPages);
    } catch (err: any) {
      console.error('Failed to load reports:', err);
      setError(err.response?.data?.message || '신고 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetail = async (reportId: number) => {
    try {
      const detail = await adminService.getReportDetail(reportId);
      setSelectedReport(detail);
      setShowDetailModal(true);
      setProcessingNote('');
    } catch (err: any) {
      console.error('Failed to load report detail:', err);
      alert(err.response?.data?.message || '신고 상세를 불러오는데 실패했습니다.');
    }
  };

  const handleProcessReport = async (status: 'APPROVED' | 'REJECTED') => {
    if (!selectedReport || !user?.id) return;

    if (!confirm(`이 신고를 ${status === 'APPROVED' ? '승인' : '거부'}하시겠습니까?`)) {
      return;
    }

    try {
      setIsProcessing(true);
      await adminService.processReport(selectedReport.id, user.id, {
        status,
        processingNote: processingNote || undefined,
        applyActions: status === 'APPROVED',
      });

      alert(`신고가 ${status === 'APPROVED' ? '승인' : '거부'}되었습니다.`);
      setShowDetailModal(false);
      setSelectedReport(null);
      loadReports();
    } catch (err: any) {
      console.error('Failed to process report:', err);
      alert(err.response?.data?.message || '신고 처리에 실패했습니다.');
    } finally {
      setIsProcessing(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return (
          <span className="px-2 py-1 text-xs font-medium bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400 rounded">
            대기중
          </span>
        );
      case 'APPROVED':
        return (
          <span className="px-2 py-1 text-xs font-medium bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 rounded">
            승인됨
          </span>
        );
      case 'REJECTED':
        return (
          <span className="px-2 py-1 text-xs font-medium bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded">
            거부됨
          </span>
        );
      default:
        return null;
    }
  };

  const getReportTypeLabel = (type: string) => {
    switch (type) {
      case 'POST':
        return '게시글';
      case 'COMMENT':
        return '댓글';
      case 'USER':
        return '사용자';
      default:
        return type;
    }
  };

  const formatDate = (dateString: string) => {
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
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">신고 관리</h1>
        <p className="mt-2 text-gray-600 dark:text-gray-400">
          사용자 신고를 검토하고 처리하세요
        </p>
      </div>

      {/* 필터 */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
        <div className="flex gap-2">
          {(['PENDING', 'APPROVED', 'REJECTED', 'ALL'] as const).map((status) => (
            <button
              key={status}
              onClick={() => {
                setFilter(status);
                setPage(0);
              }}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                filter === status
                  ? 'bg-blue-600 dark:bg-blue-500 text-white'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
              }`}
            >
              {status === 'PENDING' && '대기중'}
              {status === 'APPROVED' && '승인됨'}
              {status === 'REJECTED' && '거부됨'}
              {status === 'ALL' && '전체'}
            </button>
          ))}
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

      {/* 신고 목록 */}
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
                    신고 유형
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    신고 사유
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    신고자
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    상태
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    신고일시
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                    액션
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {reports.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-gray-500 dark:text-gray-400">
                      신고가 없습니다.
                    </td>
                  </tr>
                ) : (
                  reports.map((report) => (
                    <tr key={report.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {report.id}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {getReportTypeLabel(report.reportType)}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900 dark:text-white">
                        <div className="max-w-xs truncate">{report.reason}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                        {report.reporter.nickname || report.reporter.username}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(report.status)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                        {formatDate(report.createdAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <Button
                          onClick={() => handleViewDetail(report.id)}
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

      {/* 신고 상세 모달 */}
      {selectedReport && (
        <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedReport(null);
          }}
          title="신고 상세"
        >
          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">신고 유형</label>
              <p className="mt-1 text-gray-900 dark:text-white">
                {getReportTypeLabel(selectedReport.reportType)}
              </p>
            </div>

            <div>
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">신고 사유</label>
              <p className="mt-1 text-gray-900 dark:text-white">{selectedReport.reason}</p>
            </div>

            {selectedReport.description && (
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">상세 설명</label>
                <p className="mt-1 text-gray-900 dark:text-white whitespace-pre-wrap">
                  {selectedReport.description}
                </p>
              </div>
            )}

            <div>
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">신고자</label>
              <p className="mt-1 text-gray-900 dark:text-white">
                {selectedReport.reporter.nickname || selectedReport.reporter.username}
              </p>
            </div>

            {selectedReport.reportedUser && (
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">신고된 사용자</label>
                <p className="mt-1 text-gray-900 dark:text-white">
                  {selectedReport.reportedUser.nickname || selectedReport.reportedUser.username}
                </p>
              </div>
            )}

            {selectedReport.targetContent && (
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">신고된 콘텐츠</label>
                <div className="mt-1 p-3 bg-gray-50 dark:bg-gray-900 rounded border border-gray-200 dark:border-gray-700">
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">
                    작성자: {selectedReport.targetContent.author.nickname || selectedReport.targetContent.author.username}
                  </p>
                  <p className="text-gray-900 dark:text-white whitespace-pre-wrap">
                    {selectedReport.targetContent.content}
                  </p>
                </div>
              </div>
            )}

            <div>
              <label className="text-sm font-medium text-gray-700 dark:text-gray-300">상태</label>
              <div className="mt-1">{getStatusBadge(selectedReport.status)}</div>
            </div>

            {selectedReport.status === 'PENDING' && (
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">처리 메모</label>
                <textarea
                  value={processingNote}
                  onChange={(e) => setProcessingNote(e.target.value)}
                  placeholder="처리 사유나 메모를 입력하세요 (선택사항)"
                  rows={3}
                  className="mt-1 w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            )}

            <div className="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
              <Button
                onClick={() => setShowDetailModal(false)}
                className="bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300"
                disabled={isProcessing}
              >
                닫기
              </Button>
              {selectedReport.status === 'PENDING' && (
                <>
                  <Button
                    onClick={() => handleProcessReport('REJECTED')}
                    className="bg-red-600 dark:bg-red-700 hover:bg-red-700 dark:hover:bg-red-600 text-white"
                    disabled={isProcessing}
                  >
                    거부
                  </Button>
                  <Button
                    onClick={() => handleProcessReport('APPROVED')}
                    className="bg-green-600 dark:bg-green-700 hover:bg-green-700 dark:hover:bg-green-600 text-white"
                    disabled={isProcessing}
                    isLoading={isProcessing}
                  >
                    승인
                  </Button>
                </>
              )}
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
