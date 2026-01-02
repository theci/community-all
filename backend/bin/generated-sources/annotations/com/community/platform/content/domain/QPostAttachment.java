package com.community.platform.content.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPostAttachment is a Querydsl query type for PostAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostAttachment extends EntityPathBase<PostAttachment> {

    private static final long serialVersionUID = 1076358895L;

    public static final QPostAttachment postAttachment = new QPostAttachment("postAttachment");

    public final com.community.platform.shared.domain.QAggregateRoot _super = new com.community.platform.shared.domain.QAggregateRoot(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final StringPath fileName = createString("fileName");

    public final StringPath filePath = createString("filePath");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final EnumPath<AttachmentType> fileType = createEnum("fileType", AttachmentType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mimeType = createString("mimeType");

    public final StringPath originalName = createString("originalName");

    public final NumberPath<Long> postId = createNumber("postId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPostAttachment(String variable) {
        super(PostAttachment.class, forVariable(variable));
    }

    public QPostAttachment(Path<? extends PostAttachment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPostAttachment(PathMetadata metadata) {
        super(PostAttachment.class, metadata);
    }

}

