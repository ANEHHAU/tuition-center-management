package com.tuition.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho Nhóm/Lớp học (Group), chứa các trường phục vụ tạo link xem lịch public.
 */
@Entity
@Table(name = "study_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String courseName;

    @Column(unique = true, length = 64)
    private String publicToken;

    @Builder.Default
    @Column(nullable = false)
    private Boolean tokenEnabled = true;


    private LocalDateTime tokenExpiresAt;

    private LocalDateTime tokenCreatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.tokenEnabled == null) {
            this.tokenEnabled = true;
        }
    }
}
