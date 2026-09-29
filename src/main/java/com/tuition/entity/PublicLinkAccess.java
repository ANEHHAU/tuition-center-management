package com.tuition.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity lưu vết lịch sử truy cập Public Link của phụ huynh / học sinh.
 */
@Entity
@Table(name = "public_link_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicLinkAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String publicToken;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime accessedAt;

    @PrePersist
    protected void onCreate() {
        this.accessedAt = LocalDateTime.now();
    }
}
