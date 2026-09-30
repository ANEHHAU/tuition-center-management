package com.tuition.repository;

import com.tuition.entity.Invoice;
import com.tuition.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByStudentIdAndMonthAndYear(Long studentId, Integer month, Integer year);

    List<Invoice> findByStudentIdOrderByYearDescMonthDesc(Long studentId);

    List<Invoice> findByMonthAndYear(Integer month, Integer year);

    // Dành cho giáo viên xem invoice của học sinh do mình quản lý
    // Ở đây, học sinh do giáo viên quản lý có trường createdById = teacherId (theo Phase B)
    // Hoặc qua danh sách group. Nhưng đơn giản nhất là theo student.createdById
    @Query("SELECT i FROM Invoice i WHERE i.student.createdById = :teacherId AND i.month = :month AND i.year = :year")
    List<Invoice> findByTeacherIdAndMonthAndYear(@Param("teacherId") Long teacherId, @Param("month") Integer month, @Param("year") Integer year);

    // Danh sách nợ
    List<Invoice> findByStatusIn(List<InvoiceStatus> statuses);

    @Query("SELECT i FROM Invoice i WHERE i.student.createdById = :teacherId AND i.status IN :statuses")
    List<Invoice> findByTeacherIdAndStatusIn(@Param("teacherId") Long teacherId, @Param("statuses") List<InvoiceStatus> statuses);
}
