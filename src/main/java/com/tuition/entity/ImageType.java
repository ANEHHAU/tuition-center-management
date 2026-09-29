package com.tuition.entity;

import lombok.Getter;

/**
 * Enum định nghĩa loại ảnh và kích thước crop/resize cố định.
 */
@Getter
public enum ImageType {
    AVATAR(400, 400),
    COURSE_COVER(800, 600),
    COURSE_THUMB(300, 300);

    private final int width;
    private final int height;

    ImageType(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
