package com.community.platform.admin.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 관리자 통계 응답 DTO
 */
@Getter
@Builder
public class AdminStatisticsResponse {

    private Long totalUsers;
    private Long totalPosts;
    private Long totalComments;
    private Long totalReports;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newPostsToday;
    private Long pendingReports;
}
