package com.example.coday.repository;

import com.example.coday.model.Purchase;
import com.example.coday.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepo extends JpaRepository<Purchase, Long> {
    boolean existsByUser(User user);
}
