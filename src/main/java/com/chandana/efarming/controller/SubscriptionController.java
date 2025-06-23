package com.chandana.efarming.controller;

import com.chandana.efarming.model.Subscription;
import com.chandana.efarming.model.User;
import com.chandana.efarming.repository.SubscriptionRepository;
import com.chandana.efarming.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ Subscribe customer to farmer
    @PostMapping
    public Subscription createSubscription(@RequestBody Subscription subscription) {
        Long customerId = subscription.getCustomer().getId();
        Long farmerId = subscription.getFarmer().getId();

        if (customerId.equals(farmerId)) {
            throw new RuntimeException("Customer cannot subscribe to themselves");
        }

        // Check if users exist
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        // Check for duplicate
        if (subscriptionRepository.findByCustomerIdAndFarmerId(customerId, farmerId).isPresent()) {
            throw new RuntimeException("Already subscribed");
        }

        subscription.setCustomer(customer);
        subscription.setFarmer(farmer);
        return subscriptionRepository.save(subscription);
    }

    // ✅ Get subscriptions by customer
    @GetMapping("/customer/{id}")
    public List<Subscription> getByCustomer(@PathVariable Long id) {
        return subscriptionRepository.findByCustomerId(id);
    }

    // ✅ Get subscribers of a farmer
    @GetMapping("/farmer/{id}")
    public List<Subscription> getByFarmer(@PathVariable Long id) {
        return subscriptionRepository.findByFarmerId(id);
    }

    // ✅ Unsubscribe (delete)
    @DeleteMapping
    public String deleteSubscription(@RequestParam Long customerId, @RequestParam Long farmerId) {
        Subscription subscription = subscriptionRepository.findByCustomerIdAndFarmerId(customerId, farmerId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        subscriptionRepository.delete(subscription);
        return "Unsubscribed successfully";
    }
}
