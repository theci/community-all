package com.community.platform.engagement.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScrapFolder is a Querydsl query type for ScrapFolder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScrapFolder extends EntityPathBase<ScrapFolder> {

    private static final long serialVersionUID = -284662373L;

    public static final QScrapFolder scrapFolder = new QScrapFolder("scrapFolder");

    public final com.community.platform.shared.domain.QBaseEntity _super = new com.community.platform.shared.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QScrapFolder(String variable) {
        super(ScrapFolder.class, forVariable(variable));
    }

    public QScrapFolder(Path<? extends ScrapFolder> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScrapFolder(PathMetadata metadata) {
        super(ScrapFolder.class, metadata);
    }

}

