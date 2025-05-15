package com.example.coday.controller;

import com.example.coday.model.Company;
import com.example.coday.model.Product;
import com.example.coday.repository.CompanyRepo;
import com.example.coday.repository.ProductRepo;
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
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductRepo productRepo;
    private final CompanyRepo companyRepo;
    private static final Path UPLOAD_DIR = Paths.get("uploads");

    public AdminProductController(ProductRepo productRepo, CompanyRepo companyRepo) {
        this.productRepo = productRepo;
        this.companyRepo = companyRepo;
    }

    @PostMapping("/upload")
    public String uploadProduct(@RequestParam("name") String name,
                                @RequestParam("pointsCost") int pointsCost,
                                @RequestParam("image") MultipartFile imageFile,
                                @RequestParam("companyId") Long companyId,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Company> companyOpt = companyRepo.findById(companyId);
            if (companyOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Företaget kunde inte hittas.");
                return "redirect:/admin/dashboard#product-upload-section";
            }

            Files.createDirectories(UPLOAD_DIR);
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename().replaceAll("\\s+", "_");
            Path imagePath = UPLOAD_DIR.resolve(filename);
            Files.write(imagePath, imageFile.getBytes());

            Product product = new Product(name, pointsCost, "/uploads/" + filename, companyOpt.get());
            productRepo.save(product);
            redirectAttributes.addFlashAttribute("success", "Produkten har laddats upp!");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Fel vid uppladdning: " + e.getMessage());
        }

        return "redirect:/admin/dashboard#product-list-section";
    }

    @PostMapping("/edit")
    public String updateProduct(@RequestParam("productId") Long id,
                                @RequestParam("name") String name,
                                @RequestParam("pointsCost") int pointsCost,
                                @RequestParam("companyId") Long companyId,
                                @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productRepo.findById(id);
        Optional<Company> companyOpt = companyRepo.findById(companyId);

        if (productOpt.isEmpty() || companyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Produkt eller företag kunde inte hittas.");
            return "redirect:/admin/dashboard#product-list-section";
        }

        Product product = productOpt.get();
        product.setName(name);
        product.setPointsCost(pointsCost);
        product.setCompany(companyOpt.get());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Files.createDirectories(UPLOAD_DIR);
                String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename().replaceAll("\\s+", "_");
                Path imagePath = UPLOAD_DIR.resolve(filename);
                Files.write(imagePath, imageFile.getBytes());
                product.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Fel vid bilduppladdning.");
                return "redirect:/admin/dashboard#product-list-section";
            }
        }

        productRepo.save(product);
        redirectAttributes.addFlashAttribute("success", "Produkten har uppdaterats.");
        return "redirect:/admin/dashboard#product-list-section";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productRepo.existsById(id)) {
            productRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Produkten har tagits bort.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Produkten kunde inte hittas.");
        }
        return "redirect:/admin/dashboard#product-list-section";
    }
}