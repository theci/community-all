package com.community.platform.shared.infrastructure;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 트랜잭션 커밋 후 이벤트 처리를 위한 조합 어노테이션
 * @TransactionalEventListener + @Component 를 하나로 합침
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TransactionalEventListener
@Component
public @interface TransactionalEventHandler {
    
    /**
     * 이벤트 리스너의 순서 (낮을수록 먼저 실행)
     */
    @AliasFor(annotation = TransactionalEventListener.class, attribute = "order")
    int order() default 0;
    
    /**
     * 조건부 실행을 위한 SpEL 표현식
     */
    @AliasFor(annotation = TransactionalEventListener.class, attribute = "condition")
    String condition() default "";
}