package com.tuition.dto;

import com.tuition.entity.Course;
import com.tuition.entity.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String name,
        BigDecimal pricePerSession,
        String description,
        Long teacherId,
        String teacherName,
        String coverUrl,
        CourseStatus status,
        LocalDateTime createdAt,
        long groupCount
) {
    public static CourseResponse fromEntity(Course course, long groupCount) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getPricePerSession(),
                course.getDescription(),
                course.getTeacher().getId(),
                course.getTeacher().getFullName(),
                course.getCoverUrl(),
                course.getStatus(),
                course.getCreatedAt(),
                groupCount
        );
    }
}
