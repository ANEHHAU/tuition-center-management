package com.tuition.dto;

import com.tuition.entity.Enrollment;
import com.tuition.entity.EnrollmentStatus;

import java.time.LocalDate;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        Long groupId,
        String groupName,
        LocalDate joinDate,
        LocalDate leaveDate,
        EnrollmentStatus status
) {
    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getFullName(),
                enrollment.getGroup().getId(),
                enrollment.getGroup().getName(),
                enrollment.getJoinDate(),
                enrollment.getLeaveDate(),
                enrollment.getStatus()
        );
    }
}
