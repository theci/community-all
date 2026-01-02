package com.community.platform.engagement.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scrap_folders",
       indexes = {
           @Index(name = "idx_scrap_folder_user", columnList = "user_id, created_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapFolder extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    private ScrapFolder(Long userId, String name, String description, Boolean isDefault) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.isDefault = isDefault;
    }

    public static ScrapFolder create(Long userId, String name, String description) {
        return new ScrapFolder(userId, name, description, false);
    }

    public static ScrapFolder createDefault(Long userId) {
        return new ScrapFolder(userId, "기본 폴더", "기본 스크랩 폴더", true);
    }

    public void updateInfo(String name, String description) {
        validateName(name);
        this.name = name;
        this.description = description;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("폴더명은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("폴더명은 100자를 초과할 수 없습니다.");
        }
    }
}