package com.tuition.controller;

import com.tuition.entity.ImageType;
import com.tuition.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller xử lý upload hình ảnh (Avatar & Course Cover) lên Cloudinary.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Upload ảnh đại diện (Avatar 400x400)
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = fileStorageService.uploadWithDetails(file, ImageType.AVATAR);
        return ResponseEntity.ok(result);
    }

    /**
     * Upload ảnh bìa khóa học (Course Cover 800x600)
     */
    @PostMapping("/course-cover")
    public ResponseEntity<Map<String, String>> uploadCourseCover(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = fileStorageService.uploadWithDetails(file, ImageType.COURSE_COVER);
        return ResponseEntity.ok(result);
    }
}
