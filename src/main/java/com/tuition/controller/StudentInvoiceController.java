package com.tuition.controller;

import com.tuition.dto.InvoiceResponse;
import com.tuition.dto.PageResponse;
import com.tuition.dto.BaseSearchRequest;
import com.tuition.entity.InvoiceStatus;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.InvoiceService;
import com.tuition.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentInvoiceController {

    private final InvoiceService invoiceService;
    private final StudentService studentService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<InvoiceResponse> getInvoices(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) InvoiceStatus status,
            @ModelAttribute BaseSearchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        List<InvoiceResponse> all = invoiceService.listByStudent(getCurrentUser(userDetails).getId());
        
        if (month != null) {
            all = all.stream().filter(i -> i.month() == month).toList();
        }
        if (year != null) {
            all = all.stream().filter(i -> i.year() == year).toList();
        }
        if (status != null) {
            all = all.stream().filter(i -> i.status() == status).toList();
        }
        
        int start = request.getPage() * request.getSize();
        int end = Math.min((start + request.getSize()), all.size());
        List<InvoiceResponse> pageContent = start <= end && start < all.size() ? all.subList(start, end) : Collections.emptyList();
        
        return PageResponse.of(new PageImpl<>(pageContent, PageRequest.of(request.getPage(), request.getSize()), all.size()));
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return invoiceService.getById(id, getCurrentUser(userDetails));
    }

    @GetMapping("/current-debt")
    public Map<String, BigDecimal> getCurrentDebt(@AuthenticationPrincipal UserDetails userDetails) {
        List<InvoiceResponse> invoices = invoiceService.listByStudent(getCurrentUser(userDetails).getId());
        BigDecimal totalDebt = invoices.stream()
                .filter(i -> i.status() == InvoiceStatus.UNPAID || i.status() == InvoiceStatus.PARTIAL)
                .map(InvoiceResponse::remainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("totalDebt", totalDebt);
    }
    
    @GetMapping("/debt-history")
    public List<Map<String, Object>> getDebtHistory(@AuthenticationPrincipal UserDetails userDetails) {
        List<InvoiceResponse> invoices = invoiceService.listByStudent(getCurrentUser(userDetails).getId());
        // For simplicity, just return all invoices that have debt, or just group by month
        return invoices.stream()
                .map(i -> Map.<String, Object>of(
                        "month", String.format("%02d/%d", i.month(), i.year()),
                        "amount", i.totalAmount(),
                        "debt", i.remainingAmount(),
                        "status", i.status()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/notify-payment")
    public Map<String, String> notifyPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        studentService.notifyPayment(getCurrentUser(userDetails), id, payload);
        return Map.of("message", "Đã gửi thông báo thanh toán");
    }
}
