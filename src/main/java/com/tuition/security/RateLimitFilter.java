package com.tuition.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter Rate Limiting đơn giản dùng Bucket4j ngăn chặn DDoS cho Upload (10 req/min) & Public Link (30 req/min).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> uploadBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> publicBuckets = new ConcurrentHashMap<>();

    private Bucket createUploadBucket() {
        Bandwidth limit = Bandwidth.simple(10, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createPublicBucket() {
        Bandwidth limit = Bandwidth.simple(30, Duration.ofMinutes(1));
        return Bucket.builder().addLimit(limit).build();
    }


    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIP = getClientIP(request);

        // Rate limiting cho Upload endpoints: tối đa 10 req / phút
        if (path.startsWith("/api/files/")) {
            Bucket bucket = uploadBuckets.computeIfAbsent(clientIP, k -> createUploadBucket());
            if (!bucket.tryConsume(1)) {
                sendRateLimitError(response, "Vui lòng thử lại sau (vượt quá 10 lượt upload / phút)");
                return;
            }
        }

        // Rate limiting cho Public schedule endpoints: tối đa 30 req / phút
        if (path.startsWith("/public/")) {
            Bucket bucket = publicBuckets.computeIfAbsent(clientIP, k -> createPublicBucket());
            if (!bucket.tryConsume(1)) {
                sendRateLimitError(response, "Vui lòng thử lại sau (vượt quá 30 lượt xem public / phút)");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
