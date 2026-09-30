package com.tuition.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.tuition.entity.ImageType;
import com.tuition.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Service lưu trữ và xử lý upload file ảnh lên Cloudinary với kiểm tra Magic Bytes và Resize cố định.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file, ImageType type) {
        Map<String, String> result = uploadWithDetails(file, type);
        return result.get("url");
    }

    @Override
    public Map<String, String> uploadWithDetails(MultipartFile file, ImageType type) {
        validateFile(file);

        try {
            // Chỉ crop/resize ảnh trước khi lưu để tiết kiệm dung lượng
            Transformation incomingTransformation = new Transformation()
                    .width(type.getWidth())
                    .height(type.getHeight())
                    .crop("fill");

            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", "tuition/" + type.name().toLowerCase(),
                    "resource_type", "image",
                    "transformation", incomingTransformation,
                    "overwrite", false,
                    "unique_filename", true
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String publicId = (String) uploadResult.get("public_id");

            // Tạo Delivery URL với f_auto, q_auto để load nhanh trên web
            String optimizedUrl = cloudinary.url()
                    .secure(true)
                    .transformation(new Transformation().fetchFormat("auto").quality("auto"))
                    .generate(publicId);

            Map<String, String> response = new HashMap<>();
            response.put("url", optimizedUrl);
            response.put("publicId", publicId);

            log.info("Uploaded file successfully to Cloudinary: publicId={}, optimizedUrl={}", publicId, optimizedUrl);
            return response;

        } catch (Exception e) {
            log.error("Cloudinary upload error: ", e);
            throw new BusinessException("Lỗi trong quá trình upload ảnh lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted Cloudinary image publicId: {}", publicId);
        } catch (Exception e) {
            log.error("Cloudinary delete error: ", e);
            throw new BusinessException("Lỗi khi xóa ảnh trên Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Validate file trước khi upload: Null check, kích thước, Content-Type & Magic Bytes
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File upload không được để trống");
        }

        // Validate kích thước <= 2MB
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("Ảnh ≤ 2MB");
        }

        // Validate Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("Chỉ chấp nhận file định dạng hình ảnh");
        }

        // Kiểm tra Magic Bytes thực tế của file stream
        if (!isImageMagicBytes(file)) {
            throw new BusinessException("File upload không phải là định dạng ảnh hợp lệ (JPEG, PNG, WEBP, GIF)");
        }
    }

    /**
     * Kiểm tra Magic Bytes để xác minh file thật sự là ảnh (JPEG, PNG, GIF, WEBP)
     */
    private boolean isImageMagicBytes(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header, 0, 12);
            if (bytesRead < 4) {
                return false;
            }

            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                return true;
            }

            // PNG: 89 50 4E 47
            if ((header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50 &&
                (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47) {
                return true;
            }

            // GIF: 47 49 46 38 ("GIF8")
            if ((header[0] & 0xFF) == 0x47 && (header[1] & 0xFF) == 0x49 &&
                (header[2] & 0xFF) == 0x46 && (header[3] & 0xFF) == 0x38) {
                return true;
            }

            // WEBP: "RIFF" .... "WEBP"
            if (bytesRead >= 12 &&
                (header[0] & 0xFF) == 0x52 && (header[1] & 0xFF) == 0x49 && // 'R', 'I'
                (header[2] & 0xFF) == 0x46 && (header[3] & 0xFF) == 0x46 && // 'F', 'F'
                (header[8] & 0xFF) == 0x57 && (header[9] & 0xFF) == 0x45 && // 'W', 'E'
                (header[10] & 0xFF) == 0x42 && (header[11] & 0xFF) == 0x50) { // 'B', 'P'
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Lỗi khi đọc Magic Bytes của file: ", e);
            return false;
        }
    }
}
