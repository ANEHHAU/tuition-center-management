package com.tuition.controller;

import com.tuition.dto.InvoiceResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/invoices")
@RequiredArgsConstructor
public class StudentInvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public List<InvoiceResponse> getMyInvoices(@AuthenticationPrincipal UserDetails userDetails) {
        return invoiceService.listByStudent(getCurrentUser(userDetails).getId());
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceDetail(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return invoiceService.getById(id, getCurrentUser(userDetails));
    }

    @GetMapping("/current-debt")
    public Map<String, BigDecimal> getCurrentDebt(@AuthenticationPrincipal UserDetails userDetails) {
        List<InvoiceResponse> invoices = invoiceService.listByStudent(getCurrentUser(userDetails).getId());
        BigDecimal totalDebt = invoices.stream()
                .map(InvoiceResponse::remainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("totalDebt", totalDebt);
    }
}
