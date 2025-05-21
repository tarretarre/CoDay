package com.example.coday.repository;

import com.example.coday.model.Purchase;
import com.example.coday.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepo extends JpaRepository<Purchase, Long> {
    boolean existsByUser(User user);
    List<Purchase> findByUserOrderByDateDesc(User user);
    List<Purchase> findByProduct_Company_IdOrderByDateDesc(Long companyId);
}
