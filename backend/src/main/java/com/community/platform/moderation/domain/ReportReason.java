package com.community.platform.moderation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 신고 사유
 */
@Getter
@RequiredArgsConstructor
public enum ReportReason {
    SPAM("스팸/광고", "상업적 광고, 홍보, 스팸성 콘텐츠", 10),
    ABUSE("욕설/비방", "욕설, 비방, 모욕적인 표현", 50),
    SEXUAL("음란물", "성적인 콘텐츠, 음란물", 100),
    VIOLENCE("폭력적 콘텐츠", "폭력적이거나 잔인한 콘텐츠", 80),
    HATE_SPEECH("혐오 발언", "인종, 성별, 종교 등에 대한 혐오 발언", 100),
    MISINFORMATION("허위 정보", "거짓 정보, 허위 사실 유포", 30),
    COPYRIGHT("저작권 침해", "타인의 저작물 무단 도용", 20),
    PERSONAL_INFO("개인정보 노출", "타인의 개인정보 무단 공개", 70),
    ILLEGAL_CONTENT("불법 콘텐츠", "불법적인 콘텐츠 게시", 100),
    ETC("기타", "기타 부적절한 콘텐츠", 5);

    private final String displayName;
    private final String description;
    private final int severityScore; // 심각도 점수 (0-100)

    public boolean isHighSeverity() {
        return severityScore >= 70;
    }

    public boolean isMediumSeverity() {
        return severityScore >= 30 && severityScore < 70;
    }

    public boolean isLowSeverity() {
        return severityScore < 30;
    }
}
