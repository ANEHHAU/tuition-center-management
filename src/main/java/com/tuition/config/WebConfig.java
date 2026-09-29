package com.tuition.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Đã chuyển sang dùng Cloudinary để lưu trữ hình ảnh trực tuyến
}
