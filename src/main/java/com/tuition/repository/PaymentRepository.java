package com.tuition.repository;

import com.tuition.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    // Báo cáo doanh thu toàn hệ thống trong khoảng thời gian
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paidAt >= :from AND p.paidAt <= :to")
    BigDecimal sumAmountByPaidAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Báo cáo doanh thu của riêng giáo viên (các invoice thuộc học sinh của giáo viên đó)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.invoice.student.createdById = :teacherId AND p.paidAt >= :from AND p.paidAt <= :to")
    BigDecimal sumAmountByTeacherAndPaidAtBetween(@Param("teacherId") Long teacherId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
