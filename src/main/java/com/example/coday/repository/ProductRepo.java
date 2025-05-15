package com.example.coday.repository;

import com.example.coday.model.Company;
import com.example.coday.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {
    List<Product> findByCompany(Company company);
}
