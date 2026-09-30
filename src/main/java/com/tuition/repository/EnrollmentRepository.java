package com.tuition.repository;

import com.tuition.entity.Enrollment;
import com.tuition.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByGroupId(Long groupId);

    List<Enrollment> findByGroupIdAndStatus(Long groupId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Tìm enrollment ACTIVE của 1 học sinh tại 1 thời điểm:
     * join_date <= date AND (leave_date IS NULL OR leave_date >= date)
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId " +
           "AND e.status = 'ACTIVE' " +
           "AND e.joinDate <= :date " +
           "AND (e.leaveDate IS NULL OR e.leaveDate >= :date)")
    List<Enrollment> findActiveByStudentAndDate(@Param("studentId") Long studentId,
                                                @Param("date") LocalDate date);

    /**
     * Đếm số học sinh đang ACTIVE trong 1 group
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.group.id = :groupId AND e.status = 'ACTIVE'")
    long countActiveByGroupId(@Param("groupId") Long groupId);

    /**
     * Tìm enrollment ACTIVE của 1 học sinh trong 1 group cụ thể
     */
    Optional<Enrollment> findByStudentIdAndGroupIdAndStatus(Long studentId, Long groupId, EnrollmentStatus status);
}
