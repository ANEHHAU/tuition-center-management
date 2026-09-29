package com.tuition.service;

import com.tuition.entity.ImageType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileStorageService {
    String upload(MultipartFile file, ImageType type);
    Map<String, String> uploadWithDetails(MultipartFile file, ImageType type);
    void delete(String publicId);
}
