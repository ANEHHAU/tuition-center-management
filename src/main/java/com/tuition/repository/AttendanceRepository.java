package com.tuition.repository;

import com.tuition.entity.Attendance;
import com.tuition.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findBySessionId(Long sessionId);

    Optional<Attendance> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    List<Attendance> findByStudentIdAndSessionDateBetween(Long studentId, LocalDate from, LocalDate to);

    long countByStudentIdAndStatusAndSessionDateBetween(Long studentId, AttendanceStatus status,
                                                        LocalDate from, LocalDate to);

    void deleteBySessionId(Long sessionId);

    long countBySessionId(Long sessionId);
}
