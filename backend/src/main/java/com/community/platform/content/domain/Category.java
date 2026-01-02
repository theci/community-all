package com.community.platform.content.domain;

import com.community.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<Category> subCategories = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    private Category(Category parentCategory, String name, String description, Integer displayOrder) {
        this.parentCategory = parentCategory;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }

    public static Category create(Category parentCategory, String name, String description, Integer displayOrder) {
        validateName(name);
        return new Category(parentCategory, name, description, displayOrder);
    }

    public void updateInfo(String name, String description, Integer displayOrder) {
        validateName(name);
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isRootCategory() {
        return this.parentCategory == null;
    }

    public boolean hasSubCategories() {
        return !this.subCategories.isEmpty();
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("카테고리명은 100자를 초과할 수 없습니다.");
        }
    }
}