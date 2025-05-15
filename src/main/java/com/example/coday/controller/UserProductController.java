package com.example.coday.controller;

import com.example.coday.model.Company;
import com.example.coday.model.Product;
import com.example.coday.model.User;
import com.example.coday.repository.ProductRepo;
import com.example.coday.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user/products")
public class UserProductController {

    private final ProductRepo productRepo;

    public UserProductController(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @GetMapping
    public String getCompanyProducts(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = currentUser.getUser();
        Company company = user.getCompany();

        if (company == null) {
            model.addAttribute("error", "Du är inte kopplad till ett företag.");
            model.addAttribute("products", List.of());
        } else {
            List<Product> products = productRepo.findByCompany(company);
            model.addAttribute("products", products);
        }

        return "user-products";
    }
}