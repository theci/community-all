// 날짜 포맷팅
export function formatDate(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

export function formatDateTime(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

// 상대 시간 (예: "3분 전", "1시간 전")
export function getRelativeTime(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  const diffInMs = now.getTime() - d.getTime();
  const diffInSec = Math.floor(diffInMs / 1000);
  const diffInMin = Math.floor(diffInSec / 60);
  const diffInHour = Math.floor(diffInMin / 60);
  const diffInDay = Math.floor(diffInHour / 24);

  if (diffInSec < 60) return '방금 전';
  if (diffInMin < 60) return `${diffInMin}분 전`;
  if (diffInHour < 24) return `${diffInHour}시간 전`;
  if (diffInDay < 7) return `${diffInDay}일 전`;

  return formatDate(d);
}

// 숫자 포맷팅 (천 단위 콤마)
export function formatNumber(num: number): string {
  return num.toLocaleString('ko-KR');
}

// 숫자 축약 (예: 1000 → 1K, 1000000 → 1M)
export function abbreviateNumber(num: number): string {
  if (num < 1000) return num.toString();
  if (num < 1000000) return `${(num / 1000).toFixed(1)}K`;
  return `${(num / 1000000).toFixed(1)}M`;
}

// 텍스트 자르기
export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength) + '...';
}

// 클래스명 결합
export function classNames(...classes: (string | boolean | undefined)[]): string {
  return classes.filter(Boolean).join(' ');
}
