package com.example.coday.repository;

import com.example.coday.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepo extends JpaRepository<Company, Long> {
    boolean existsByName(String name);

    List<Company> findByNameContainingIgnoreCase(String name);
}
