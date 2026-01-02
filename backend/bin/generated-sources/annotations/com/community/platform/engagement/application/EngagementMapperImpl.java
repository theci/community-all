package com.community.platform.engagement.application;

import com.community.platform.engagement.domain.PostLike;
import com.community.platform.engagement.domain.PostScrap;
import com.community.platform.engagement.domain.ScrapFolder;
import com.community.platform.engagement.dto.PostLikeResponse;
import com.community.platform.engagement.dto.PostScrapResponse;
import com.community.platform.engagement.dto.ScrapFolderResponse;
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
public class EngagementMapperImpl implements EngagementMapper {

    @Override
    public PostLikeResponse toPostLikeResponse(PostLike postLike) {
        if ( postLike == null ) {
            return null;
        }

        PostLikeResponse.PostLikeResponseBuilder postLikeResponse = PostLikeResponse.builder();

        postLikeResponse.createdAt( postLike.getCreatedAt() );
        postLikeResponse.id( postLike.getId() );
        postLikeResponse.postId( postLike.getPostId() );

        return postLikeResponse.build();
    }

    @Override
    public List<PostLikeResponse> toPostLikeResponseList(List<PostLike> postLikes) {
        if ( postLikes == null ) {
            return null;
        }

        List<PostLikeResponse> list = new ArrayList<PostLikeResponse>( postLikes.size() );
        for ( PostLike postLike : postLikes ) {
            list.add( toPostLikeResponse( postLike ) );
        }

        return list;
    }

    @Override
    public PostScrapResponse toPostScrapResponse(PostScrap postScrap) {
        if ( postScrap == null ) {
            return null;
        }

        PostScrapResponse.PostScrapResponseBuilder postScrapResponse = PostScrapResponse.builder();

        postScrapResponse.createdAt( postScrap.getCreatedAt() );
        postScrapResponse.id( postScrap.getId() );

        return postScrapResponse.build();
    }

    @Override
    public List<PostScrapResponse> toPostScrapResponseList(List<PostScrap> postScraps) {
        if ( postScraps == null ) {
            return null;
        }

        List<PostScrapResponse> list = new ArrayList<PostScrapResponse>( postScraps.size() );
        for ( PostScrap postScrap : postScraps ) {
            list.add( toPostScrapResponse( postScrap ) );
        }

        return list;
    }

    @Override
    public ScrapFolderResponse toScrapFolderResponse(ScrapFolder scrapFolder) {
        if ( scrapFolder == null ) {
            return null;
        }

        ScrapFolderResponse.ScrapFolderResponseBuilder scrapFolderResponse = ScrapFolderResponse.builder();

        scrapFolderResponse.createdAt( scrapFolder.getCreatedAt() );
        scrapFolderResponse.description( scrapFolder.getDescription() );
        scrapFolderResponse.id( scrapFolder.getId() );
        scrapFolderResponse.isDefault( scrapFolder.getIsDefault() );
        scrapFolderResponse.name( scrapFolder.getName() );
        scrapFolderResponse.userId( scrapFolder.getUserId() );

        return scrapFolderResponse.build();
    }

    @Override
    public List<ScrapFolderResponse> toScrapFolderResponseList(List<ScrapFolder> scrapFolders) {
        if ( scrapFolders == null ) {
            return null;
        }

        List<ScrapFolderResponse> list = new ArrayList<ScrapFolderResponse>( scrapFolders.size() );
        for ( ScrapFolder scrapFolder : scrapFolders ) {
            list.add( toScrapFolderResponse( scrapFolder ) );
        }

        return list;
    }
}
