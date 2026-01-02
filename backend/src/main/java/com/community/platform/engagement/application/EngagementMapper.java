package com.community.platform.engagement.application;

import com.community.platform.engagement.domain.PostLike;
import com.community.platform.engagement.domain.PostScrap;
import com.community.platform.engagement.domain.ScrapFolder;
import com.community.platform.engagement.dto.*;
import com.community.platform.content.dto.PostSummaryResponse;
import com.community.platform.user.dto.UserSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Engagement 도메인 객체와 DTO 간 매핑을 담당하는 MapStruct 매퍼
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EngagementMapper {

    // ========== PostLike 매핑 ==========

    /**
     * PostLike 엔티티를 PostLikeResponse DTO로 변환
     */
    @Mapping(target = "user", ignore = true) // 별도 설정 필요
    PostLikeResponse toPostLikeResponse(PostLike postLike);

    /**
     * PostLike 리스트를 PostLikeResponse 리스트로 변환
     */
    List<PostLikeResponse> toPostLikeResponseList(List<PostLike> postLikes);

    // ========== PostScrap 매핑 ==========

    /**
     * PostScrap 엔티티를 PostScrapResponse DTO로 변환
     * postId, userId, folderId는 자동 매핑
     * post와 scrapFolder는 별도로 설정해야 함
     */
    @Mapping(target = "folderId", expression = "java(postScrap.getScrapFolder() != null ? postScrap.getScrapFolder().getId() : null)")
    @Mapping(target = "post", ignore = true) // 별도 설정 필요
    @Mapping(target = "scrapFolder", ignore = true) // 별도 설정 필요
    PostScrapResponse toPostScrapResponse(PostScrap postScrap);

    /**
     * PostScrap 리스트를 PostScrapResponse 리스트로 변환
     */
    List<PostScrapResponse> toPostScrapResponseList(List<PostScrap> postScraps);

    // ========== ScrapFolder 매핑 ==========

    /**
     * ScrapFolder 엔티티를 ScrapFolderResponse DTO로 변환
     */
    @Mapping(target = "scrapCount", ignore = true) // 별도 설정 필요
    ScrapFolderResponse toScrapFolderResponse(ScrapFolder scrapFolder);

    /**
     * ScrapFolder 리스트를 ScrapFolderResponse 리스트로 변환
     */
    List<ScrapFolderResponse> toScrapFolderResponseList(List<ScrapFolder> scrapFolders);

    // ========== 복합 매핑 메서드 ==========

    /**
     * PostLike와 사용자 정보를 함께 사용하여 완전한 PostLikeResponse 생성
     */
    default PostLikeResponse toPostLikeResponseWithUser(PostLike postLike, UserSummaryResponse user) {
        return PostLikeResponse.builder()
                .id(postLike.getId())
                .postId(postLike.getPostId())
                .user(user)
                .createdAt(postLike.getCreatedAt())
                .build();
    }

    /**
     * PostScrap과 관련 정보를 함께 사용하여 완전한 PostScrapResponse 생성
     */
    default PostScrapResponse toPostScrapResponseWithDetails(PostScrap postScrap,
                                                           PostSummaryResponse post,
                                                           ScrapFolderResponse scrapFolder) {
        return PostScrapResponse.builder()
                .id(postScrap.getId())
                .postId(postScrap.getPostId())
                .userId(postScrap.getUserId())
                .folderId(postScrap.getScrapFolder() != null ? postScrap.getScrapFolder().getId() : null)
                .post(post)
                .scrapFolder(scrapFolder)
                .createdAt(postScrap.getCreatedAt())
                .build();
    }

    /**
     * ScrapFolder와 스크랩 수를 함께 사용하여 완전한 ScrapFolderResponse 생성
     */
    default ScrapFolderResponse toScrapFolderResponseWithCount(ScrapFolder scrapFolder, Long scrapCount) {
        return ScrapFolderResponse.builder()
                .id(scrapFolder.getId())
                .userId(scrapFolder.getUserId())
                .name(scrapFolder.getName())
                .description(scrapFolder.getDescription())
                .isDefault(scrapFolder.getIsDefault())
                .createdAt(scrapFolder.getCreatedAt())
                .scrapCount(scrapCount != null ? scrapCount : 0L)
                .build();
    }

    /**
     * ScrapFolder 목록과 각 폴더별 스크랩 수를 함께 사용하여 응답 생성
     */
    default List<ScrapFolderResponse> toScrapFolderResponseListWithCounts(
            List<ScrapFolder> scrapFolders, List<Long> scrapCounts) {
        
        if (scrapFolders == null) return null;
        
        List<ScrapFolderResponse> responses = new java.util.ArrayList<>();
        for (int i = 0; i < scrapFolders.size(); i++) {
            ScrapFolder folder = scrapFolders.get(i);
            Long count = (scrapCounts != null && i < scrapCounts.size()) ? scrapCounts.get(i) : 0L;
            responses.add(toScrapFolderResponseWithCount(folder, count));
        }
        
        return responses;
    }
}