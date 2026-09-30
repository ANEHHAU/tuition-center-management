package com.tuition.config;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình kết nối Cloudinary cho việc lưu trữ và xử lý hình ảnh.
 * Ưu tiên dùng CLOUDINARY_URL nếu có, nếu không thì dùng 3 tham số riêng lẻ.
 */
@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    @Value("${cloudinary.cloud-name:demo}")
    private String cloudName;

    @Value("${cloudinary.api-key:123456789}")
    private String apiKey;

    @Value("${cloudinary.api-secret:secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Ưu tiên dùng CLOUDINARY_URL (format: cloudinary://api_key:api_secret@cloud_name)
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank() && cloudinaryUrl.startsWith("cloudinary://")) {
            log.info("Khởi tạo Cloudinary từ URL: cloudinary://***@{}", 
                     cloudinaryUrl.contains("@") ? cloudinaryUrl.substring(cloudinaryUrl.lastIndexOf("@") + 1) : "unknown");
            return new Cloudinary(cloudinaryUrl);
        }

        // Fallback: dùng 3 tham số riêng lẻ
        log.info("Khởi tạo Cloudinary từ tham số riêng lẻ: cloud_name={}", cloudName);
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
