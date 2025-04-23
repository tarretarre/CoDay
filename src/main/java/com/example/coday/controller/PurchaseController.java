package com.example.coday.controller;

import com.example.coday.model.Purchase;
import com.example.coday.model.User;
import com.example.coday.model.Product;
import com.example.coday.repository.PurchaseRepo;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.ProductRepo;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseRepo purchaseRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public PurchaseController(PurchaseRepo purchaseRepo, UserRepo userRepo, ProductRepo productRepo) {
        this.purchaseRepo = purchaseRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @PostMapping("/{userId}/{productId}")
    public String makePurchase(@PathVariable Long userId, @PathVariable Long productId) {
        Optional<User> userOpt = userRepo.findById(userId);
        Optional<Product> productOpt = productRepo.findById(productId);

        if (userOpt.isEmpty()) return "Användare hittades inte.";
        if (productOpt.isEmpty()) return "Produkt hittades inte.";

        User user = userOpt.get();
        Product product = productOpt.get();

        if (user.getPoints() < product.getPointsCost()) {
            return "Inte tillräckligt med poäng för att köpa produkten.";
        }

        user.setPoints(user.getPoints() - product.getPointsCost());
        userRepo.save(user);

        Purchase purchase = new Purchase(user, product);
        purchaseRepo.save(purchase);

        return "Köp genomfört! " + product.getName() + " köptes.";
    }

    @GetMapping
    public List<Purchase> getAllPurchases() {
        return purchaseRepo.findAll();
    }
}
