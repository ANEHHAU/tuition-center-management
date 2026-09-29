package com.tuition.service;

import com.tuition.entity.Group;
import com.tuition.exception.BusinessException;
import com.tuition.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service quản lý sinh token ngẫu nhiên, bật/tắt và xác thực Public Link xem lịch học cho nhóm.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublicLinkService {

    private final GroupRepository groupRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Sinh token random 32 bytes -> Base64 URL-safe (không padding ~43 ký tự)
     */
    @Transactional
    public String generateToken(Group group) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        group.setPublicToken(token);
        group.setTokenEnabled(true);
        group.setTokenCreatedAt(LocalDateTime.now());
        group.setTokenExpiresAt(LocalDateTime.now().plusDays(90));

        groupRepository.save(group);
        log.info("Generated public link token for group id={}: token={}", group.getId(), token);
        return token;
    }

    /**
     * Tắt link chia sẻ (Revoke token)
     */
    @Transactional
    public void revokeToken(Group group) {
        group.setTokenEnabled(false);
        groupRepository.save(group);
        log.info("Revoked public link token for group id={}", group.getId());
    }

    /**
     * Tải lại (Regenerate) token mới: vô hiệu hóa token cũ và sinh token mới
     */
    @Transactional
    public String regenerateToken(Group group) {
        revokeToken(group);
        return generateToken(group);
    }

    /**
     * Kiểm tra tính hợp lệ của token
     */
    public Group validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Token không hợp lệ");
        }

        Group group = groupRepository.findByPublicToken(token)
                .orElseThrow(() -> new BusinessException("Link lịch học không tồn tại"));

        if (Boolean.FALSE.equals(group.getTokenEnabled())) {
            throw new BusinessException("Link lịch học đã bị ngắt chia sẻ");
        }

        if (group.getTokenExpiresAt() != null && group.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Link lịch học đã hết hạn");
        }

        return group;
    }
}
