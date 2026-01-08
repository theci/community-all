package com.community.platform.content.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 검색 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PostSearchRequest {

    @Size(max = 100, message = "검색 키워드는 100자를 초과할 수 없습니다")
    private String keyword; // 검색 키워드

    private String searchType = "ALL"; // 검색 유형 (TITLE, CONTENT, ALL, AUTHOR)

    private Long categoryId; // 카테고리 ID 필터

    @Size(max = 5, message = "태그 필터는 최대 5개까지 가능합니다")
    private List<String> tagNames; // 태그 필터

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate; // 시작 날짜

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate; // 종료 날짜

    private Long authorId; // 작성자 ID 필터

    private String sortBy = "publishedAt"; // 정렬 기준 (publishedAt, viewCount, likeCount)

    private String sortDirection = "desc"; // 정렬 방향 (asc, desc)
}