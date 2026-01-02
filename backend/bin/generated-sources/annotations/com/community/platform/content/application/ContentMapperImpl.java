package com.community.platform.content.application;

import com.community.platform.content.domain.Category;
import com.community.platform.content.domain.Comment;
import com.community.platform.content.domain.Post;
import com.community.platform.content.domain.Tag;
import com.community.platform.content.dto.CategoryResponse;
import com.community.platform.content.dto.CommentResponse;
import com.community.platform.content.dto.PostResponse;
import com.community.platform.content.dto.PostSummaryResponse;
import com.community.platform.content.dto.TagResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T11:21:06+0000",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ContentMapperImpl implements ContentMapper {

    @Override
    public PostResponse toPostResponse(Post post) {
        if ( post == null ) {
            return null;
        }

        PostResponse.PostResponseBuilder postResponse = PostResponse.builder();

        postResponse.commentCount( post.getCommentCount() );
        postResponse.content( post.getContent() );
        postResponse.contentType( post.getContentType() );
        postResponse.createdAt( post.getCreatedAt() );
        postResponse.id( post.getId() );
        postResponse.isNoticePost( post.getIsNoticePost() );
        postResponse.likeCount( post.getLikeCount() );
        postResponse.publishedAt( post.getPublishedAt() );
        postResponse.status( post.getStatus() );
        postResponse.title( post.getTitle() );
        postResponse.updatedAt( post.getUpdatedAt() );
        postResponse.viewCount( post.getViewCount() );

        return postResponse.build();
    }

    @Override
    public PostSummaryResponse toPostSummaryResponse(Post post) {
        if ( post == null ) {
            return null;
        }

        PostSummaryResponse.PostSummaryResponseBuilder postSummaryResponse = PostSummaryResponse.builder();

        postSummaryResponse.commentCount( post.getCommentCount() );
        postSummaryResponse.id( post.getId() );
        postSummaryResponse.isNoticePost( post.getIsNoticePost() );
        postSummaryResponse.likeCount( post.getLikeCount() );
        postSummaryResponse.publishedAt( post.getPublishedAt() );
        postSummaryResponse.title( post.getTitle() );
        postSummaryResponse.viewCount( post.getViewCount() );

        return postSummaryResponse.build();
    }

    @Override
    public CommentResponse toCommentResponse(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentResponse.CommentResponseBuilder commentResponse = CommentResponse.builder();

        commentResponse.postId( commentPostId( comment ) );
        commentResponse.parentCommentId( commentParentCommentId( comment ) );
        commentResponse.content( comment.getContent() );
        commentResponse.createdAt( comment.getCreatedAt() );
        commentResponse.depth( comment.getDepth() );
        commentResponse.id( comment.getId() );
        commentResponse.likeCount( comment.getLikeCount() );
        commentResponse.status( comment.getStatus() );
        commentResponse.updatedAt( comment.getUpdatedAt() );

        return commentResponse.build();
    }

    @Override
    public List<CommentResponse> toCommentResponseList(List<Comment> comments) {
        if ( comments == null ) {
            return null;
        }

        List<CommentResponse> list = new ArrayList<CommentResponse>( comments.size() );
        for ( Comment comment : comments ) {
            list.add( toCommentResponse( comment ) );
        }

        return list;
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.parentCategoryId( categoryParentCategoryId( category ) );
        categoryResponse.createdAt( category.getCreatedAt() );
        categoryResponse.description( category.getDescription() );
        categoryResponse.displayOrder( category.getDisplayOrder() );
        categoryResponse.id( category.getId() );
        categoryResponse.isActive( category.getIsActive() );
        categoryResponse.name( category.getName() );

        return categoryResponse.build();
    }

    @Override
    public List<CategoryResponse> toCategoryResponseList(List<Category> categories) {
        if ( categories == null ) {
            return null;
        }

        List<CategoryResponse> list = new ArrayList<CategoryResponse>( categories.size() );
        for ( Category category : categories ) {
            list.add( toCategoryResponse( category ) );
        }

        return list;
    }

    @Override
    public TagResponse toTagResponse(Tag tag) {
        if ( tag == null ) {
            return null;
        }

        TagResponse.TagResponseBuilder tagResponse = TagResponse.builder();

        tagResponse.color( tag.getColor() );
        tagResponse.id( tag.getId() );
        tagResponse.name( tag.getName() );
        tagResponse.usageCount( tag.getUsageCount() );

        return tagResponse.build();
    }

    @Override
    public List<TagResponse> toTagResponseList(List<Tag> tags) {
        if ( tags == null ) {
            return null;
        }

        List<TagResponse> list = new ArrayList<TagResponse>( tags.size() );
        for ( Tag tag : tags ) {
            list.add( toTagResponse( tag ) );
        }

        return list;
    }

    private Long commentPostId(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        Post post = comment.getPost();
        if ( post == null ) {
            return null;
        }
        Long id = post.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long commentParentCommentId(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        Comment parentComment = comment.getParentComment();
        if ( parentComment == null ) {
            return null;
        }
        Long id = parentComment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long categoryParentCategoryId(Category category) {
        if ( category == null ) {
            return null;
        }
        Category parentCategory = category.getParentCategory();
        if ( parentCategory == null ) {
            return null;
        }
        Long id = parentCategory.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
