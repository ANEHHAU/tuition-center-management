package com.tuition.exception;

/**
 * Exception tùy chỉnh dùng cho các lỗi nghiệp vụ trong hệ thống.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
