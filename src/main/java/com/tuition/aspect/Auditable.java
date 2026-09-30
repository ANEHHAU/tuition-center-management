package com.tuition.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu một method cần ghi Audit Log tự động.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action(); // CREATE, UPDATE, DELETE...
    String entityType(); // USER, COURSE...
}
