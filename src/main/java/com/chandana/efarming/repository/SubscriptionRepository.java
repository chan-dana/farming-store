package com.chandana.efarming.repository;

import com.chandana.efarming.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByCustomerId(Long customerId);
    List<Subscription> findByFarmerId(Long farmerId);
    Optional<Subscription> findByCustomerIdAndFarmerId(Long customerId, Long farmerId);
}
