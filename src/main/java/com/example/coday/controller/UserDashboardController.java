package com.example.coday.controller;

import com.example.coday.dto.VisitDTO;
import com.example.coday.model.Purchase;
import com.example.coday.model.User;
import com.example.coday.model.Product;
import com.example.coday.repository.PurchaseRepo;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.VisitRepo;
import com.example.coday.repository.ProductRepo;
import com.example.coday.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserDashboardController {
    private final VisitRepo visitRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final PurchaseRepo purchaseRepo;


    public UserDashboardController(VisitRepo visitRepo, ProductRepo productRepo, UserRepo userRepo, PasswordEncoder passwordEncoder, PurchaseRepo purchaseRepo) {
        this.visitRepo = visitRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.purchaseRepo = purchaseRepo;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = currentUser.getUser();

        Optional<com.example.coday.model.Visit> currentVisitOpt = visitRepo.findFirstByUserAndCheckOutTimeIsNullOrderByCheckInTimeDesc(user);
        com.example.coday.model.Visit currentVisit = currentVisitOpt.orElse(null);

        List<com.example.coday.model.Visit> rawVisits = visitRepo.findByUserOrderByCheckInTimeDesc(user);
        List<VisitDTO> visits = rawVisits.stream()
                .map(VisitDTO::new)
                .toList();

        List<Product> availableRewards = productRepo.findByCompany(user.getCompany());
        List<Purchase> purchases = purchaseRepo.findByUserOrderByDateDesc(user);

        model.addAttribute("user", user);
        model.addAttribute("currentVisit", currentVisit);
        model.addAttribute("visits", visits);
        model.addAttribute("points", user.getPoints());
        model.addAttribute("availableRewards", availableRewards);
        model.addAttribute("purchases", purchases);

        return "user-dashboard";
    }


    @PostMapping("/update-profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @RequestParam("newPassword") String newPassword) {
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            User user = currentUser.getUser();
            String encoded = passwordEncoder.encode(newPassword);
            user.setPassword(encoded);
            userRepo.save(user);
        }
        return "redirect:/user/dashboard";
    }
    @PostMapping("/redeem/{productId}")
    public String redeemProduct(@PathVariable Long productId,
                                @AuthenticationPrincipal CustomUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {

        Optional<Product> productOpt = productRepo.findById(productId);
        User user = currentUser.getUser();

        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Produkten hittades inte.");
            return "redirect:/user/dashboard";
        }

        Product product = productOpt.get();

        if (!product.getCompany().getId().equals(user.getCompany().getId())) {
            redirectAttributes.addFlashAttribute("error", "Produkten tillhör inte ditt företag.");
            return "redirect:/user/dashboard";
        }

        if (user.getPoints() < product.getPointsCost()) {
            redirectAttributes.addFlashAttribute("error", "Du har inte tillräckligt med poäng.");
            return "redirect:/user/dashboard";
        }

        user.setPoints(user.getPoints() - product.getPointsCost());
        userRepo.save(user);

        Purchase purchase = new Purchase(user, product);
        purchaseRepo.save(purchase);

        redirectAttributes.addFlashAttribute("success", "Du har löst in: " + product.getName());
        return "redirect:/user/dashboard";
    }

}
