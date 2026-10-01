package com.tuition.service;

import com.tuition.aspect.Auditable;
import com.tuition.dto.PaymentRequest;
import com.tuition.dto.PaymentResponse;
import com.tuition.entity.*;
import com.tuition.exception.BusinessException;
import com.tuition.repository.InvoiceRepository;
import com.tuition.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    @Auditable(action = "CREATE", entityType = "PAYMENT")
    public PaymentResponse record(PaymentRequest req, User currentUser) {
        Invoice invoice = invoiceRepository.findById(req.invoiceId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn"));

        if (currentUser.getRole() == Role.TEACHER && !invoice.getStudent().getCreatedById().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền thao tác trên hóa đơn này");
        }

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thanh toán phải lớn hơn 0");
        }
        if (req.amount().compareTo(remaining) > 0) {
            throw new BusinessException("Số tiền thanh toán vượt quá số tiền còn nợ (" + remaining + ")");
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(req.amount())
                .method(req.method())
                .recordedBy(currentUser)
                .note(req.note())
                .build();

        Payment saved = paymentRepository.save(payment);

        invoice.setPaidAmount(invoice.getPaidAmount().add(req.amount()));
        
        if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIAL);
        }
        
        invoiceRepository.save(invoice);

        return PaymentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }
}
