package com.community.platform.content.application;

import com.community.platform.content.domain.*;
import com.community.platform.content.dto.*;
import com.community.platform.user.dto.UserSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Content 도메인 객체와 DTO 간 매핑을 담당하는 MapStruct 매퍼
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ContentMapper {

    // ========== Post 매핑 ==========
    
    /**
     * Post 엔티티를 PostResponse DTO로 변환
     */
    @Mapping(target = "author", ignore = true) // 별도 설정 필요
    @Mapping(target = "category", ignore = true) // 별도 설정 필요
    @Mapping(target = "tags", ignore = true) // 별도 설정 필요
    @Mapping(target = "isLikedByUser", ignore = true) // 별도 설정 필요
    @Mapping(target = "isScrappedByUser", ignore = true) // 별도 설정 필요
    PostResponse toPostResponse(Post post);

    /**
     * Post 엔티티를 PostSummaryResponse DTO로 변환
     */
    @Mapping(target = "author", ignore = true) // 별도 설정 필요
    @Mapping(target = "category", ignore = true) // 별도 설정 필요
    @Mapping(target = "summary", ignore = true) // 별도 설정 필요 (content의 일부)
    @Mapping(target = "tags", ignore = true) // 별도 설정 필요
    @Mapping(target = "isLikedByUser", ignore = true) // 별도 설정 필요
    @Mapping(target = "isScrappedByUser", ignore = true) // 별도 설정 필요
    PostSummaryResponse toPostSummaryResponse(Post post);

    // ========== Comment 매핑 ==========

    /**
     * Comment 엔티티를 CommentResponse DTO로 변환
     */
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    @Mapping(target = "author", ignore = true) // 별도 설정 필요
    @Mapping(target = "replies", ignore = true) // 별도 설정 필요
    @Mapping(target = "isLikedByUser", ignore = true) // 별도 설정 필요
    @Mapping(target = "isAuthor", ignore = true) // 별도 설정 필요
    CommentResponse toCommentResponse(Comment comment);

    /**
     * Comment 리스트를 CommentResponse 리스트로 변환
     */
    List<CommentResponse> toCommentResponseList(List<Comment> comments);

    // ========== Category 매핑 ==========

    /**
     * Category 엔티티를 CategoryResponse DTO로 변환
     */
    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(target = "subCategories", ignore = true) // 별도 설정 필요
    @Mapping(target = "postCount", ignore = true) // 별도 설정 필요
    CategoryResponse toCategoryResponse(Category category);

    /**
     * Category 리스트를 CategoryResponse 리스트로 변환
     */
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    // ========== Tag 매핑 ==========

    /**
     * Tag 엔티티를 TagResponse DTO로 변환
     */
    TagResponse toTagResponse(Tag tag);

    /**
     * Tag 리스트를 TagResponse 리스트로 변환
     */
    List<TagResponse> toTagResponseList(List<Tag> tags);

    // ========== 복합 매핑 메서드 ==========

    /**
     * Post와 관련 정보를 함께 사용하여 완전한 PostResponse 생성
     */
    default PostResponse toPostResponseWithDetails(Post post, UserSummaryResponse author, 
                                                 CategoryResponse category, List<TagResponse> tags,
                                                 Boolean isLikedByUser, Boolean isScrappedByUser) {
        PostResponse response = toPostResponse(post);
        return PostResponse.builder()
                .id(response.getId())
                .author(author)
                .category(category)
                .title(response.getTitle())
                .content(response.getContent())
                .contentType(response.getContentType())
                .status(response.getStatus())
                .isNoticePost(response.getIsNoticePost())
                .viewCount(response.getViewCount())
                .likeCount(response.getLikeCount())
                .commentCount(response.getCommentCount())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .publishedAt(response.getPublishedAt())
                .tags(tags)
                .isLikedByUser(isLikedByUser)
                .isScrappedByUser(isScrappedByUser)
                .build();
    }

    /**
     * Post와 관련 정보를 함께 사용하여 PostSummaryResponse 생성
     */
    default PostSummaryResponse toPostSummaryResponseWithDetails(Post post, UserSummaryResponse author,
                                                               CategoryResponse category, List<TagResponse> tags,
                                                               Boolean isLikedByUser, Boolean isScrappedByUser) {
        // 내용 요약 생성 (처음 100자)
        String summary = post.getContent();
        if (summary != null && summary.length() > 100) {
            summary = summary.substring(0, 100) + "...";
        }

        return PostSummaryResponse.builder()
                .id(post.getId())
                .author(author)
                .category(category)
                .title(post.getTitle())
                .summary(summary)
                .isNoticePost(post.getIsNoticePost())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .publishedAt(post.getPublishedAt())
                .tags(tags != null && tags.size() > 3 ? tags.subList(0, 3) : tags) // 최대 3개 태그만
                .isLikedByUser(isLikedByUser)
                .isScrappedByUser(isScrappedByUser)
                .build();
    }

    /**
     * Comment와 관련 정보를 함께 사용하여 완전한 CommentResponse 생성
     */
    default CommentResponse toCommentResponseWithDetails(Comment comment, UserSummaryResponse author,
                                                        List<CommentResponse> replies, Boolean isLikedByUser, 
                                                        Boolean isAuthor) {
        CommentResponse response = toCommentResponse(comment);
        return CommentResponse.builder()
                .id(response.getId())
                .postId(response.getPostId())
                .parentCommentId(response.getParentCommentId())
                .author(author)
                .content(response.getContent())
                .status(response.getStatus())
                .likeCount(response.getLikeCount())
                .depth(response.getDepth())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .replies(replies)
                .isLikedByUser(isLikedByUser)
                .isAuthor(isAuthor)
                .build();
    }

    /**
     * Category와 하위 카테고리를 함께 사용하여 완전한 CategoryResponse 생성
     */
    default CategoryResponse toCategoryResponseWithSubCategories(Category category, 
                                                               List<CategoryResponse> subCategories, 
                                                               Long postCount) {
        CategoryResponse response = toCategoryResponse(category);
        return CategoryResponse.builder()
                .id(response.getId())
                .parentCategoryId(response.getParentCategoryId())
                .name(response.getName())
                .description(response.getDescription())
                .displayOrder(response.getDisplayOrder())
                .isActive(response.getIsActive())
                .createdAt(response.getCreatedAt())
                .subCategories(subCategories)
                .postCount(postCount)
                .build();
    }
}