'use client';

import { useState, FormEvent } from 'react';
import { Button } from '@/components/ui';

interface CommentFormProps {
  onSubmit: (content: string) => Promise<void>;
  onCancel?: () => void;
  initialValue?: string;
  placeholder?: string;
  submitLabel?: string;
}

export function CommentForm({
  onSubmit,
  onCancel,
  initialValue = '',
  placeholder = '댓글을 작성하세요',
  submitLabel = '작성',
}: CommentFormProps) {
  const [content, setContent] = useState(initialValue);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!content.trim()) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }

    try {
      setIsSubmitting(true);
      await onSubmit(content.trim());
      setContent('');
    } catch (error) {
      console.error('Failed to submit comment:', error);
      alert('댓글 작성에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder={placeholder}
        rows={3}
        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
        disabled={isSubmitting}
      />
      <div className="flex items-center gap-2 justify-end">
        {onCancel && (
          <Button
            type="button"
            onClick={onCancel}
            className="bg-gray-200 hover:bg-gray-300 text-gray-700"
            disabled={isSubmitting}
          >
            취소
          </Button>
        )}
        <Button
          type="submit"
          className="bg-blue-600 hover:bg-blue-700 text-white"
          disabled={isSubmitting}
          isLoading={isSubmitting}
        >
          {isSubmitting ? '작성 중...' : submitLabel}
        </Button>
      </div>
    </form>
  );
}
