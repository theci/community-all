package com.community.platform.content.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_tag_name", columnNames = "name")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    private Tag(String name, String color) {
        this.name = name;
        this.color = color;
        this.usageCount = 0L;
    }

    public static Tag create(String name, String color) {
        validateName(name);
        validateColor(color);
        return new Tag(name, color);
    }

    public void updateColor(String color) {
        validateColor(color);
        this.color = color;
    }

    public void increaseUsageCount() {
        this.usageCount++;
    }

    public void decreaseUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("태그명은 필수입니다.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("태그명은 50자를 초과할 수 없습니다.");
        }
        if (!name.matches("^[가-힣a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("태그명은 한글, 영문, 숫자, _, -만 사용 가능합니다.");
        }
    }

    private static void validateColor(String color) {
        if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("색상은 HEX 형식이어야 합니다. (예: #FF0000)");
        }
    }
}