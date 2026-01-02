package com.community.platform.shared.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답을 위한 공통 DTO
 * Spring Data의 Page 객체를 클라이언트 친화적 형태로 변환
 */
@Getter
public class PageResponse<T> {
    
    private final List<T> content; // 실제 데이터 목록
    private final PageInfo pageInfo; // 페이징 정보

    public PageResponse(List<T> content, PageInfo pageInfo) {
        this.content = content;
        this.pageInfo = pageInfo;
    }

    /**
     * Spring Data Page 객체로부터 PageResponse 생성
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageInfo pageInfo = PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
        
        return new PageResponse<>(page.getContent(), pageInfo);
    }

    /**
     * 페이징 정보를 담는 내부 클래스
     */
    @Getter
    @lombok.Builder
    public static class PageInfo {
        private final int currentPage; // 현재 페이지 번호 (0부터 시작)
        private final int totalPages; // 전체 페이지 수
        private final long totalElements; // 전체 요소 수
        private final int pageSize; // 페이지 크기
        private final boolean hasNext; // 다음 페이지 존재 여부
        private final boolean hasPrevious; // 이전 페이지 존재 여부
        private final boolean isFirst; // 첫 번째 페이지 여부
        private final boolean isLast; // 마지막 페이지 여부
    }
}