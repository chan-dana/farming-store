package com.chandana.efarming.controller;

import com.chandana.efarming.model.Order;
import com.chandana.efarming.model.Payment;
import com.chandana.efarming.repository.OrderRepository;
import com.chandana.efarming.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ✅ Create payment entry
    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        if (payment.getOrder() == null || payment.getOrder().getId() == null) {
            throw new RuntimeException("Order ID is required");
        }

        Order order = orderRepository.findById(payment.getOrder().getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        payment.setOrder(order);

        // Auto set timestamp if success
        if ("success".equalsIgnoreCase(payment.getPaymentStatus())) {
            payment.setPaidAt(LocalDateTime.now());
        }

        return paymentRepository.save(payment);
    }

    // ✅ Get all payments
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // ✅ Get payments by order ID
    @GetMapping("/order/{orderId}")
    public List<Payment> getByOrderId(@PathVariable Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // ✅ Delete payment
    @DeleteMapping("/{id}")
    public String deletePayment(@PathVariable Long id) {
        if (!paymentRepository.existsById(id)) {
            return "Payment not found";
        }
        paymentRepository.deleteById(id);
        return "Payment deleted";
    }
}
