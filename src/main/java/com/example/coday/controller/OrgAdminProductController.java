package com.example.coday.controller;

import com.example.coday.model.Company;
import com.example.coday.model.Product;
import com.example.coday.repository.ProductRepo;
import com.example.coday.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/org-admin/products")
public class OrgAdminProductController {

    private final ProductRepo productRepo;
    private static final Path UPLOAD_DIR = Paths.get("uploads");

    public OrgAdminProductController(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @PostMapping("/upload")
    public String uploadProduct(@RequestParam("name") String name,
                                @RequestParam("pointsCost") int pointsCost,
                                @RequestParam("image") MultipartFile imageFile,
                                @AuthenticationPrincipal CustomUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        try {
            Company company = currentUser.getUser().getCompany();
            if (company == null) {
                redirectAttributes.addFlashAttribute("error", "Inget företag kopplat till ditt konto.");
                return "redirect:/org-admin/dashboard#product-upload-section";
            }

            Files.createDirectories(UPLOAD_DIR);
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename().replaceAll("\\s+", "_");
            Path imagePath = UPLOAD_DIR.resolve(filename);
            Files.write(imagePath, imageFile.getBytes());

            Product product = new Product(name, pointsCost, "/uploads/" + filename, company);
            productRepo.save(product);
            redirectAttributes.addFlashAttribute("success", "Produkten har laddats upp!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Fel vid uppladdning: " + e.getMessage());
        }
        return "redirect:/org-admin/dashboard#product-upload-section";
    }

    @PostMapping("/edit")
    public String editProduct(@RequestParam("productId") Long id,
                              @RequestParam("name") String name,
                              @RequestParam("pointsCost") int pointsCost,
                              @RequestParam(value = "image", required = false) MultipartFile imageFile,
                              @AuthenticationPrincipal CustomUserDetails currentUser,
                              RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productRepo.findById(id);
        Company company = currentUser.getUser().getCompany();

        if (productOpt.isEmpty() || company == null) {
            redirectAttributes.addFlashAttribute("error", "Produkt eller företag kunde inte hittas.");
            return "redirect:/org-admin/dashboard#product-list-section";
        }

        Product product = productOpt.get();
        if (!product.getCompany().getId().equals(company.getId())) {
            redirectAttributes.addFlashAttribute("error", "Du har inte behörighet att redigera denna produkt.");
            return "redirect:/org-admin/dashboard#product-list-section";
        }

        product.setName(name);
        product.setPointsCost(pointsCost);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Files.createDirectories(UPLOAD_DIR);
                String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path imagePath = UPLOAD_DIR.resolve(filename);
                Files.write(imagePath, imageFile.getBytes());
                product.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Fel vid uppladdning av ny bild.");
                return "redirect:/org-admin/dashboard#product-list-section";
            }
        }

        productRepo.save(product);
        redirectAttributes.addFlashAttribute("success", "Produkten har uppdaterats.");
        return "redirect:/org-admin/dashboard#product-list-section";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productRepo.findById(id);
        Company company = currentUser.getUser().getCompany();

        if (productOpt.isEmpty() || company == null ||
                !productOpt.get().getCompany().getId().equals(company.getId())) {
            redirectAttributes.addFlashAttribute("error", "Du har inte behörighet att ta bort denna produkt.");
            return "redirect:/org-admin/dashboard#product-list-section";
        }

        productRepo.delete(productOpt.get());
        redirectAttributes.addFlashAttribute("success", "Produkten har tagits bort.");
        return "redirect:/org-admin/dashboard#product-list-section";
    }
}