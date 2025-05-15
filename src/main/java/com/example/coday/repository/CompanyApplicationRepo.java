package com.example.coday.repository;

import com.example.coday.model.CompanyApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyApplicationRepo extends JpaRepository<CompanyApplication, Long> {
    boolean existsByOrgNumber(String orgNumber);
}
