// 신고 사유
export enum ReportReason {
  SPAM = 'SPAM',
  ABUSE = 'ABUSE',
  SEXUAL = 'SEXUAL',
  VIOLENCE = 'VIOLENCE',
  HATE_SPEECH = 'HATE_SPEECH',
  MISINFORMATION = 'MISINFORMATION',
  COPYRIGHT = 'COPYRIGHT',
  PERSONAL_INFO = 'PERSONAL_INFO',
  ILLEGAL_CONTENT = 'ILLEGAL_CONTENT',
  ETC = 'ETC'
}

// 신고 대상 유형
export enum ReportTargetType {
  POST = 'POST',
  COMMENT = 'COMMENT',
  CHAT = 'CHAT',
  USER = 'USER'
}

// 신고 생성 요청
export interface ReportCreateRequest {
  targetType: ReportTargetType;
  targetId: number;
  reason: ReportReason;
  description?: string;
}

// 신고 사유 옵션 (UI용)
export interface ReportReasonOption {
  value: ReportReason;
  label: string;
  description: string;
}

// 신고 사유 상수 (UI 표시용)
export const REPORT_REASON_OPTIONS: ReportReasonOption[] = [
  {
    value: ReportReason.SPAM,
    label: '스팸/광고',
    description: '상업적 광고, 홍보, 스팸성 콘텐츠'
  },
  {
    value: ReportReason.ABUSE,
    label: '욕설/비방',
    description: '욕설, 비방, 모욕적인 표현'
  },
  {
    value: ReportReason.SEXUAL,
    label: '음란물',
    description: '성적인 콘텐츠, 음란물'
  },
  {
    value: ReportReason.VIOLENCE,
    label: '폭력적 콘텐츠',
    description: '폭력적이거나 잔인한 콘텐츠'
  },
  {
    value: ReportReason.HATE_SPEECH,
    label: '혐오 발언',
    description: '인종, 성별, 종교 등에 대한 혐오 발언'
  },
  {
    value: ReportReason.MISINFORMATION,
    label: '허위 정보',
    description: '거짓 정보, 허위 사실 유포'
  },
  {
    value: ReportReason.COPYRIGHT,
    label: '저작권 침해',
    description: '타인의 저작물 무단 도용'
  },
  {
    value: ReportReason.PERSONAL_INFO,
    label: '개인정보 노출',
    description: '타인의 개인정보 무단 공개'
  },
  {
    value: ReportReason.ILLEGAL_CONTENT,
    label: '불법 콘텐츠',
    description: '불법적인 콘텐츠 게시'
  },
  {
    value: ReportReason.ETC,
    label: '기타',
    description: '기타 부적절한 콘텐츠'
  }
];
