'use client';

import { useEffect } from 'react';
import { useSearchParams } from 'next/navigation';

export function TokenHandler() {
  const searchParams = useSearchParams();

  useEffect(() => {
    const token = searchParams.get('token');
    if (token) {
      localStorage.setItem('accessToken', token);
      // URL에서 token 파라미터 제거
      const url = new URL(window.location.href);
      url.searchParams.delete('token');
      window.history.replaceState({}, '', url.toString());
      // 페이지 새로고침해서 인증 상태 반영
      window.location.reload();
    }
  }, [searchParams]);

  return null;
}
