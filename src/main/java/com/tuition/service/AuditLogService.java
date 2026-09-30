package com.tuition.service;

import com.tuition.dto.AuditLogResponse;
import com.tuition.dto.PageResponse;
import com.tuition.entity.AuditLog;
import com.tuition.entity.User;
import com.tuition.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String entityType, Long entityId, String oldValue, String newValue, User currentUser, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);
            auditLog.setPerformedBy(currentUser);
            
            if (request != null) {
                auditLog.setIpAddress(request.getRemoteAddr());
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
            log.info("AUDIT LOG: User {} {} {} id={}", currentUser.getUsername(), action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to write audit log", e);
        }
    }

    public PageResponse<AuditLogResponse> getLogs(Long userId, String entityType, LocalDateTime from, LocalDateTime to, int page, int size) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("performedBy").get("id"), userId));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("entityType"), entityType));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("performedAt"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("performedAt"), to));
            }
            return predicate;
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<AuditLog> logPage = auditLogRepository.findAll(spec, pageable);

        return PageResponse.of(
                logPage.getContent().stream().map(AuditLogResponse::fromEntity).toList(),
                logPage.getNumber(),
                logPage.getSize(),
                logPage.getTotalElements(),
                logPage.getTotalPages(),
                logPage.isFirst(),
                logPage.isLast()
        );
    }
}
