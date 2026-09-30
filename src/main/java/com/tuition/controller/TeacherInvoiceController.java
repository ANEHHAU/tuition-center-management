package com.tuition.controller;

import com.tuition.dto.*;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.InvoiceService;
import com.tuition.service.PaymentService;
import com.tuition.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherInvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final TeacherService teacherService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<InvoiceResponse> getInvoices(
            @ModelAttribute BaseSearchRequest req,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.searchInvoices(getCurrentUser(userDetails), month, year, status, req.getKeyword(), req.getPage(), req.getSize(), req.getSortBy(), req.getSortDir());
    }

    @PostMapping("/generate")
    public List<InvoiceResponse> generateInvoices(
            @RequestBody @Valid GenerateInvoiceRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        // Note: the original service did not have studentIds array. Just generate for all if null/empty
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

    @PostMapping("/{id}/payments")
    public PaymentResponse recordPayment(
            @PathVariable Long id,
            @RequestBody @Valid PaymentRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return paymentService.record(req, getCurrentUser(userDetails));
    }
    
    @PostMapping("/payments")
    public PaymentResponse recordPaymentNoId(
            @RequestBody @Valid PaymentRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return paymentService.record(req, getCurrentUser(userDetails));
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return invoiceService.getById(id, getCurrentUser(userDetails));
    }
}
