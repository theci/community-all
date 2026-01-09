'use client';

import { useState } from 'react';
import Modal from '@/components/ui/Modal';
import Select from '@/components/ui/Select';
import Textarea from '@/components/ui/Textarea';
import { Button } from '@ddd3/design-system';
import { reportService } from '@/lib/services';
import {
  ReportTargetType,
  ReportReason,
  REPORT_REASON_OPTIONS,
  type ReportCreateRequest,
} from '@ddd3/types';
import { useAuth } from '@/lib/hooks';

interface ReportModalProps {
  isOpen: boolean;
  onClose: () => void;
  targetType: ReportTargetType;
  targetId: number;
  targetTitle?: string;
}

export function ReportModal({
  isOpen,
  onClose,
  targetType,
  targetId,
  targetTitle,
}: ReportModalProps) {
  const { user } = useAuth();
  const [reason, setReason] = useState<ReportReason | ''>('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!user?.id) {
      alert('로그인이 필요합니다.');
      return;
    }

    if (!reason) {
      setError('신고 사유를 선택해주세요.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const request: ReportCreateRequest = {
        targetType,
        targetId,
        reason: reason as ReportReason,
        description: description.trim() || undefined,
      };

      await reportService.createReport(request, user.id);

      alert('신고가 접수되었습니다. 관리자가 검토 후 조치하겠습니다.');
      handleClose();
    } catch (err: any) {
      console.error('Failed to create report:', err);
      const errorMessage =
        err.response?.data?.message || '신고 접수에 실패했습니다.';
      setError(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    setReason('');
    setDescription('');
    setError(null);
    onClose();
  };

  const getTargetTypeLabel = () => {
    switch (targetType) {
      case ReportTargetType.POST:
        return '게시글';
      case ReportTargetType.COMMENT:
        return '댓글';
      case ReportTargetType.CHAT:
        return '쪽지';
      case ReportTargetType.USER:
        return '사용자';
      default:
        return '콘텐츠';
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={`${getTargetTypeLabel()} 신고`}
      size="md"
    >
      <form onSubmit={handleSubmit}>
        <div className="space-y-4">
          {/* 신고 대상 정보 */}
          {targetTitle && (
            <div className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
              <p className="text-sm text-gray-600 dark:text-gray-400">
                신고 대상:{' '}
                <span className="font-medium text-gray-900 dark:text-white">
                  {targetTitle}
                </span>
              </p>
            </div>
          )}

          {/* 신고 사유 선택 */}
          <Select
            label="신고 사유"
            required
            value={reason}
            onChange={(e) => {
              setReason(e.target.value as ReportReason);
              setError(null);
            }}
            options={[
              { value: '', label: '사유를 선택해주세요', disabled: true },
              ...REPORT_REASON_OPTIONS.map((opt) => ({
                value: opt.value,
                label: `${opt.label} - ${opt.description}`,
              })),
            ]}
            error={error && !reason ? '신고 사유를 선택해주세요' : undefined}
          />

          {/* 상세 설명 */}
          <Textarea
            label="상세 설명 (선택)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="신고 사유에 대한 상세한 설명을 작성해주세요."
            rows={5}
            maxLength={500}
            showCharCount
            helperText="구체적인 내용을 작성하시면 신속한 처리에 도움이 됩니다."
          />

          {/* 에러 메시지 */}
          {error && (
            <div className="p-3 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-lg">
              <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
            </div>
          )}

          {/* 안내 메시지 */}
          <div className="p-3 bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800 rounded-lg">
            <p className="text-xs text-blue-600 dark:text-blue-400">
              • 허위 신고 시 불이익을 받을 수 있습니다.
              <br />
              • 신고 내용은 관리자만 확인할 수 있습니다.
              <br />• 처리 결과는 알림으로 전달됩니다.
            </p>
          </div>
        </div>

        {/* 버튼 */}
        <div className="flex justify-end gap-3 mt-6">
          <Button
            type="button"
            onClick={handleClose}
            variant="ghost"
            disabled={submitting}
            className="bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300"
          >
            취소
          </Button>
          <Button
            type="submit"
            disabled={submitting || !reason}
            className="bg-red-600 dark:bg-red-700 hover:bg-red-700 dark:hover:bg-red-600 text-white"
          >
            {submitting ? '신고 중...' : '신고하기'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
