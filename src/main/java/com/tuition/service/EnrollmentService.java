package com.tuition.service;

import com.tuition.dto.EnrollmentResponse;
import com.tuition.entity.EnrollmentStatus;
import com.tuition.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý Ghi danh (Enrollment) học sinh vào nhóm.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    /**
     * Lấy các group mà học sinh đang học tại một ngày cụ thể
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getActiveEnrollments(Long studentId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return enrollmentRepository.findActiveByStudentAndDate(studentId, targetDate).stream()
                .map(EnrollmentResponse::fromEntity)
                .toList();
    }

    /**
     * Lấy danh sách học sinh đang học trong nhóm tại một ngày cụ thể (dành cho điểm danh)
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentsInGroupAtDate(Long groupId, LocalDate date) {
        // Tạm thời trả về list học sinh active, Phase C sẽ query theo ngày join/leave chính xác
        return enrollmentRepository.findByGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVE).stream()
                .map(EnrollmentResponse::fromEntity)
                .toList();
    }
}
