package com.community.platform.content.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 태그 응답 DTO
 */
@Getter
@Builder
public class TagResponse {

    private Long id; // 태그 ID
    
    private String name; // 태그명
    
    private String color; // 태그 색상 (HEX 코드)
    
    private Long usageCount; // 사용 횟수
}