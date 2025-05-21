package com.example.coday.repository;

import com.example.coday.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepo extends JpaRepository<Company, Long> {

    boolean existsByOrgNumber(String orgNumber);
    Optional<Company> findByRegistrationToken(String registrationToken);
}
