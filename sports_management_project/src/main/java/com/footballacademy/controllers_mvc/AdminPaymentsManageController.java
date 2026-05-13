package com.footballacademy.controllers_mvc;

import com.footballacademy.services.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public
class AdminPaymentsManageController {
    private final PaymentService paymentService;
    public AdminPaymentsManageController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @PostMapping("/payments/{id}/mark-paid")
    public String markPaid(
    @PathVariable Long id) {
        paymentService.markAsPaid(id);
        return "redirect:/admin/view/payments?paid=true";
    }
    @PostMapping("/payments/{id}/delete")
    public String delete(
    @PathVariable Long id) {
        paymentService.deletePayment(id);
        return "redirect:/admin/view/payments?deleted=true";
    }
}
