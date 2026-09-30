package com.tuition.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    // ObjectMapper static để convert args thành JSON
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return; // Không log nếu không có user
            }

            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser == null) return;

            HttpServletRequest request = null;
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
                request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            }

            String action = auditable.action();
            String entityType = auditable.entityType();
            
            // Cố gắng lấy entityId từ kết quả trả về nếu có hàm getId()
            Long entityId = extractEntityId(result);
            if (entityId == null) {
                // Nếu kết quả không có, thử tìm arg đầu tiên là Long (thường là id cho update/delete)
                Object[] args = joinPoint.getArgs();
                if (args.length > 0 && args[0] instanceof Long) {
                    entityId = (Long) args[0];
                }
            }

            // Ghi JSON của argument (Request DTO) làm newValue (hoặc id làm oldValue nếu xóa)
            String newValue = null;
            String oldValue = null;
            Object[] args = joinPoint.getArgs();
            
            if (action.equalsIgnoreCase("DELETE")) {
                oldValue = "ID: " + entityId;
            } else if (args.length > 0) {
                // Lấy argument chứa data (thường là arg cuối hoặc arg thứ 2)
                Object dataArg = args.length > 1 ? args[1] : args[0];
                if (!(dataArg instanceof Long) && !(dataArg instanceof User)) { // Bỏ qua id và current user
                     try {
                         newValue = mapper.writeValueAsString(dataArg);
                     } catch (Exception e) {
                         newValue = dataArg.toString();
                     }
                }
            }

            auditLogService.log(action, entityType, entityId, oldValue, newValue, currentUser, request);

        } catch (Exception e) {
            log.error("Lỗi khi ghi Audit Log qua AOP: {}", e.getMessage(), e);
        }
    }

    private Long extractEntityId(Object obj) {
        if (obj == null) return null;
        try {
            Method getIdMethod = obj.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(obj);
            if (id instanceof Long) {
                return (Long) id;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
