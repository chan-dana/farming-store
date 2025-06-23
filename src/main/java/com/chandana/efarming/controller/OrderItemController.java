package com.chandana.efarming.controller;

import com.chandana.efarming.model.Crop;
import com.chandana.efarming.model.Order;
import com.chandana.efarming.model.OrderItem;
import com.chandana.efarming.repository.CropRepository;
import com.chandana.efarming.repository.OrderItemRepository;
import com.chandana.efarming.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CropRepository cropRepository;

    // ✅ Create OrderItem
    @PostMapping
    public OrderItem createOrderItem(@RequestBody OrderItem orderItem) {
        if (orderItem.getOrder() == null || orderItem.getOrder().getId() == null) {
            throw new RuntimeException("Order ID is required");
        }
        if (orderItem.getCrop() == null || orderItem.getCrop().getId() == null) {
            throw new RuntimeException("Crop ID is required");
        }

        Order order = orderRepository.findById(orderItem.getOrder().getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Crop crop = cropRepository.findById(orderItem.getCrop().getId())
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        orderItem.setOrder(order);
        orderItem.setCrop(crop);

        if (orderItem.getQuantity() <= 0 || orderItem.getPrice() == null || orderItem.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid quantity or price");
        }

        return orderItemRepository.save(orderItem);
    }

    // ✅ Get all order items
    @GetMapping
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    // ✅ Get by Order ID
    @GetMapping("/order/{orderId}")
    public List<OrderItem> getItemsByOrderId(@PathVariable Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // ✅ Delete item
    @DeleteMapping("/{id}")
    public String deleteOrderItem(@PathVariable Long id) {
        if (!orderItemRepository.existsById(id)) {
            return "OrderItem not found";
        }
        orderItemRepository.deleteById(id);
        return "OrderItem deleted";
    }
}
