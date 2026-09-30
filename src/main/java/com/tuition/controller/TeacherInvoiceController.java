package com.tuition.controller;

import com.tuition.dto.*;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.InvoiceService;
import com.tuition.service.PaymentService;
import com.tuition.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/invoices")
@RequiredArgsConstructor
public class TeacherInvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final ReportService reportService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public List<InvoiceResponse> getInvoices(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        return invoiceService.listByTeacherAndMonth(currentUser.getId(), month, year);
    }

    @PostMapping("/generate")
    public List<InvoiceResponse> generateInvoices(
            @RequestBody @Valid GenerateInvoiceRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        return invoiceService.generateForTeacher(currentUser.getId(), req.month(), req.year(), currentUser);
    }

    @PostMapping("/{id}/finalize")
    public InvoiceResponse finalizeInvoice(@PathVariable Long id) {
        return invoiceService.finalize(id);
    }

    @PostMapping("/{id}/regenerate")
    public InvoiceResponse regenerateInvoice(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return invoiceService.regenerate(id, getCurrentUser(userDetails));
    }

    @PostMapping("/payments")
    public PaymentResponse recordPayment(
            @RequestBody @Valid PaymentRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return paymentService.record(req, getCurrentUser(userDetails));
    }

    @GetMapping("/reports/revenue")
    public RevenueReportResponse getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal UserDetails userDetails) {
        return reportService.revenueByTeacher(getCurrentUser(userDetails).getId(), from, to);
    }

    @GetMapping("/reports/debt")
    public List<DebtReportResponse> getDebtReport(@AuthenticationPrincipal UserDetails userDetails) {
        return reportService.debtReport(getCurrentUser(userDetails).getId());
    }
}
