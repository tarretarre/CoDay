package com.example.coday.controller;

import com.example.coday.model.User;
import com.example.coday.repository.PurchaseRepo;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.VisitRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/org-admin")
public class OrgAdminController {

    private final UserRepo userRepo;
    private final VisitRepo visitRepo;
    private final PurchaseRepo purchaseRepo;

    public OrgAdminController(UserRepo userRepo, VisitRepo visitRepo, PurchaseRepo purchaseRepo) {
        this.userRepo = userRepo;
        this.visitRepo = visitRepo;
        this.purchaseRepo = purchaseRepo;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null || loggedInUser.getRole() != User.Role.ORG_ADMIN) {
            return "redirect:/login";
        }

        Long companyId = loggedInUser.getCompany() != null ? loggedInUser.getCompany().getId() : null;
        List<User> users = userRepo.findByCompanyId(companyId);

        for (User u : users) {
            boolean hasHistory = visitRepo.existsByUser(u) || purchaseRepo.existsByUser(u);
            u.setCanBeDeleted(!hasHistory && u.getPoints() == 0);
        }

        model.addAttribute("companyName", loggedInUser.getCompany().getName());
        model.addAttribute("registrationLink", "/register?companyId=" + companyId);
        model.addAttribute("users", users);

        return "org-admin-dashboard";
    }

    @PostMapping("/update-role")
    public String updateUserRole(@RequestParam Long userId,
                                 @RequestParam String newRole,
                                 RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Användare hittades inte.");
            return "redirect:/org-admin/dashboard";
        }

        User user = userOpt.get();

        try {
            user.setRole(User.Role.valueOf(newRole));
            userRepo.save(user);
            redirectAttributes.addFlashAttribute("success", "Rollen har uppdaterats.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Ogiltig roll.");
        }

        return "redirect:/org-admin/dashboard";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Long userId,
                             RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Användare hittades inte.");
            return "redirect:/org-admin/dashboard";
        }

        User user = userOpt.get();

        boolean hasHistory = visitRepo.existsByUser(user) || purchaseRepo.existsByUser(user);

        if (hasHistory || user.getPoints() > 0) {
            redirectAttributes.addFlashAttribute("error", "Användaren kan inte tas bort.");
            return "redirect:/org-admin/dashboard";
        }

        userRepo.delete(user);
        redirectAttributes.addFlashAttribute("success", "Användaren har tagits bort.");

        return "redirect:/org-admin/dashboard";
    }
}
