package com.chandana.efarming.controller;

import com.chandana.efarming.model.Order;
import com.chandana.efarming.model.User;
import com.chandana.efarming.repository.OrderRepository;
import com.chandana.efarming.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Create Order
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        if (order.getCustomer() == null || order.getCustomer().getId() == null) {
            throw new RuntimeException("Customer ID is required");
        }

        User customer = userRepository.findById(order.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        order.setCustomer(customer);
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total amount must be positive");
        }

        return orderRepository.save(order);
    }

    // ✅ Get All Orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ Get Orders by Customer
    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // ✅ Update Status
    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!List.of("pending", "paid", "shipped", "delivered").contains(status)) {
            throw new RuntimeException("Invalid status value");
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }

    // ✅ Delete Order
    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            return "Order not found";
        }
        orderRepository.deleteById(id);
        return "Order deleted";
    }
}
