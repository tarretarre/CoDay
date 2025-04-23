package com.example.coday.controller;

import com.example.coday.model.Product;
import com.example.coday.model.User;
import com.example.coday.model.Visit;
import com.example.coday.repository.ProductRepo;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.VisitRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DashboardController {

    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final VisitRepo visitRepo;

    public DashboardController(UserRepo userRepo, ProductRepo productRepo, VisitRepo visitRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.visitRepo = visitRepo;
    }

    @GetMapping("/dashboard/{userId}")
    public String getDashboard(@PathVariable Long userId, Model model) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Användare hittades inte.");
            return "error";
        }

        List<Product> products = productRepo.findAll();
        Visit currentVisit = visitRepo.findFirstByUserAndCheckOutTimeIsNullOrderByCheckInTimeDesc(user).orElse(null);

        model.addAttribute("user", user);
        model.addAttribute("products", products);
        model.addAttribute("currentVisit", currentVisit);

        return "dashboard";
    }

    @PostMapping("/purchase/{userId}/{productId}")
    public String makePurchaseFromDashboard(@PathVariable Long userId, @PathVariable Long productId) {
        User user = userRepo.findById(userId).orElse(null);
        Product product = productRepo.findById(productId).orElse(null);

        if (user != null && product != null && user.getPoints() >= product.getPointsCost()) {
            user.setPoints(user.getPoints() - product.getPointsCost());
            userRepo.save(user);
        }

        return "redirect:/dashboard/" + userId;
    }
}

