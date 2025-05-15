package com.example.coday.repository;

import com.example.coday.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepo extends JpaRepository<Company, Long> {
    boolean existsByName(String name);

    boolean existsByOrgNumber(String orgNumber);

    List<Company> findByNameContainingIgnoreCase(String name);

    Optional<Company> findByRegistrationToken(String registrationToken);
}
