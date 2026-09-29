package com.tuition.dto;

import com.tuition.entity.Group;
import com.tuition.entity.GroupStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String name,
        Long courseId,
        String courseName,
        Long teacherId,
        String teacherName,
        LocalDate startDate,
        LocalDate endDate,
        GroupStatus status,
        long studentCount,
        String publicToken,
        Boolean tokenEnabled,
        LocalDateTime tokenExpiresAt
) {
    public static GroupResponse fromEntity(Group group, long studentCount) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCourse().getId(),
                group.getCourse().getName(),
                group.getTeacher().getId(),
                group.getTeacher().getFullName(),
                group.getStartDate(),
                group.getEndDate(),
                group.getStatus(),
                studentCount,
                group.getPublicToken(),
                group.getTokenEnabled(),
                group.getTokenExpiresAt()
        );
    }
}
