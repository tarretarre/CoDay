package com.example.coday.controller;

import com.example.coday.model.Company;
import com.example.coday.model.User;
import com.example.coday.repository.CompanyRepo;
import com.example.coday.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

@Controller
public class RegistrationTokenController {

    private final CompanyRepo companyRepo;

    public RegistrationTokenController(CompanyRepo companyRepo) {
        this.companyRepo = companyRepo;
    }

    @PostMapping("/org-admin/generate-token")
    public String generateOrgAdminToken(@AuthenticationPrincipal CustomUserDetails currentUser,
                                        RedirectAttributes redirectAttributes) {
        User admin = currentUser.getUser();

        if (admin.getCompany() == null) {
            redirectAttributes.addFlashAttribute("error", "Företag saknas.");
            return "redirect:/org-admin/dashboard";
        }

        String newToken = UUID.randomUUID().toString();
        admin.getCompany().setRegistrationToken(newToken);
        companyRepo.save(admin.getCompany());

        redirectAttributes.addFlashAttribute("success", "Registreringslänk har genererats.");
        return "redirect:/org-admin/dashboard";
    }

    @PostMapping("/admin/generate-token")
    public String generateAdminToken(@RequestParam Long companyId,
                                     @AuthenticationPrincipal CustomUserDetails currentUser,
                                     RedirectAttributes redirectAttributes) {
        if (currentUser.getUser().getRole() != User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Åtkomst nekad.");
            return "redirect:/admin/dashboard";
        }

        Optional<Company> companyOpt = companyRepo.findById(companyId);
        if (companyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Företag hittades inte.");
            return "redirect:/admin/dashboard";
        }

        Company company = companyOpt.get();
        String newToken = UUID.randomUUID().toString();
        company.setRegistrationToken(newToken);
        companyRepo.save(company);

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String fullLink = baseUrl + "/register?token=" + newToken;

        redirectAttributes.addFlashAttribute("generatedToken", newToken);
        redirectAttributes.addFlashAttribute("generatedCompany", company.getName());
        redirectAttributes.addFlashAttribute("generatedLink", fullLink);

        return "redirect:/admin/dashboard";
    }
}
