package com.community.platform.engagement.domain;

import com.community.platform.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_scraps",
       indexes = {
           @Index(name = "idx_post_scrap_user_created", columnList = "user_id, created_at"),
           @Index(name = "idx_post_scrap_user_post", columnList = "user_id, post_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostScrap extends AggregateRoot {

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrap_folder_id")
    private ScrapFolder scrapFolder;

    private PostScrap(Long postId, Long userId, ScrapFolder scrapFolder) {
        this.postId = postId;
        this.userId = userId;
        this.scrapFolder = scrapFolder;
    }

    public static PostScrap create(Long postId, Long userId, ScrapFolder scrapFolder, Long postAuthorId) {
        PostScrap scrap = new PostScrap(postId, userId, scrapFolder);

        // 스크랩 생성 이벤트 발행 (알림용)
        scrap.addDomainEvent(new ScrapCreatedEvent(
                scrap.getId(),
                postId,
                postAuthorId,
                userId
        ));

        return scrap;
    }

    // 하위 호환성을 위한 오버로드 메서드 (기존 코드 호환)
    public static PostScrap create(Long postId, Long userId, ScrapFolder scrapFolder) {
        return new PostScrap(postId, userId, scrapFolder);
    }

    public void updateScrapFolder(ScrapFolder newScrapFolder) {
        this.scrapFolder = newScrapFolder;
    }
}
