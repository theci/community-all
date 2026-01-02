package com.community.platform.engagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 스크랩 폴더 응답 DTO
 */
@Getter
@Builder
public class ScrapFolderResponse {

    private Long id; // 폴더 ID
    
    private Long userId; // 폴더 소유자 ID
    
    private String name; // 폴더명
    
    private String description; // 폴더 설명
    
    private Boolean isDefault; // 기본 폴더 여부
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 생성 시간
    
    private Long scrapCount; // 폴더 내 스크랩 수 (통계용)
}