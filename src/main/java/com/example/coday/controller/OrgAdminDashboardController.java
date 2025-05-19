package com.example.coday.controller;

import com.example.coday.model.Product;
import com.example.coday.model.Purchase;
import com.example.coday.model.User;
import com.example.coday.model.Visit;
import com.example.coday.repository.ProductRepo;
import com.example.coday.repository.PurchaseRepo;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.VisitRepo;
import com.example.coday.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/org-admin")
public class OrgAdminDashboardController {

    private final UserRepo userRepo;
    private final VisitRepo visitRepo;
    private final PurchaseRepo purchaseRepo;
    private final ProductRepo productRepo;

    private final PasswordEncoder passwordEncoder;

    public OrgAdminDashboardController(UserRepo userRepo, VisitRepo visitRepo, PurchaseRepo purchaseRepo, ProductRepo productRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.visitRepo = visitRepo;
        this.purchaseRepo = purchaseRepo;
        this.productRepo = productRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails currentUser,
                                Model model,
                                @RequestParam(value = "success", required = false) String success,
                                @RequestParam(value = "error", required = false) String error) {
        User loggedInUser = currentUser.getUser();

        if (loggedInUser.getRole() != User.Role.ORG_ADMIN) {
            return "redirect:/?error";
        }

        Long companyId = loggedInUser.getCompany() != null ? loggedInUser.getCompany().getId() : null;
        List<User> allUsers = userRepo.findByCompanyIdAndApprovedTrue(companyId);

        Map<Long, Visit> activeVisits = new HashMap<>();
        Map<Long, String> checkInTimes = new HashMap<>();

        for (User user : allUsers) {
            boolean hasHistory = visitRepo.existsByUser(user) || purchaseRepo.existsByUser(user);
            user.setCanBeDeleted(!hasHistory && user.getPoints() == 0);

            visitRepo.findByUserAndCheckOutTimeIsNull(user).ifPresent(visit -> {
                activeVisits.put(user.getId(), visit);
                checkInTimes.put(user.getId(), visit.getCheckInTime().toString());
            });
        }

        List<User> roleRequests = userRepo.findAllByRole(User.Role.USER).stream()
                .filter(u -> !u.isApproved())
                .filter(u -> u.getAdminComment() != null && !u.getAdminComment().isBlank())
                .filter(u -> u.getCompany() != null && u.getCompany().getId().equals(companyId))
                .toList();

        List<Product> products = productRepo.findByCompany(loggedInUser.getCompany());

        List<Purchase> purchases = purchaseRepo.findByProduct_Company_IdOrderByDateDesc(companyId);

        model.addAttribute("admin", loggedInUser);
        model.addAttribute("companyName", loggedInUser.getCompany().getName());
        model.addAttribute("registrationLink", "/register?token=" + loggedInUser.getCompany().getRegistrationToken());
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("activeVisits", activeVisits);
        model.addAttribute("checkInTimes", checkInTimes);
        model.addAttribute("roleRequests", roleRequests);
        model.addAttribute("products", products);
        model.addAttribute("company", loggedInUser.getCompany());
        model.addAttribute("purchases", purchases);
        model.addAttribute("success", success);
        model.addAttribute("error", error);

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

    @PostMapping("/approve-role")
    public String approveRole(@RequestParam Long userId,
                              @RequestParam String role,
                              RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Användare hittades inte.");
            return "redirect:/org-admin/dashboard";
        }

        User user = userOpt.get();

        try {
            user.setRole(User.Role.valueOf(role));
            user.setApproved(true);
            user.setAdminComment(null);
            userRepo.save(user);
            redirectAttributes.addFlashAttribute("success", "Rollen har uppdaterats.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Ogiltig roll.");
        }

        return "redirect:/org-admin/dashboard#role-section";
    }

    @PostMapping("/update-user")
    public String updateUser(@RequestParam Long userId,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam String role,
                             @RequestParam(required = false) String newPassword,
                             RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Användare hittades inte.");
            return "redirect:/org-admin/dashboard";
        }

        User user = userOpt.get();

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim());

        try {
            user.setRole(User.Role.valueOf(role));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Ogiltig roll.");
            return "redirect:/org-admin/dashboard";
        }

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepo.save(user);
        redirectAttributes.addFlashAttribute("success", "Användaruppgifter uppdaterades.");
        return "redirect:/org-admin/dashboard";
    }

    @GetMapping("/edit-user")
    public String editUser(@RequestParam Long userId,
                           @AuthenticationPrincipal CustomUserDetails currentUser,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Användare hittades inte.");
            return "redirect:/org-admin/dashboard";
        }

        User user = userOpt.get();

        if (!user.getCompany().getId().equals(currentUser.getUser().getCompany().getId())) {
            redirectAttributes.addFlashAttribute("error", "Du har inte behörighet att redigera denna användare.");
            return "redirect:/org-admin/dashboard";
        }

        model.addAttribute("selectedUser", user);
        return showDashboard(currentUser, model, null, null);
    }

    @PostMapping("/purchases/{id}/toggle-delivery")
    public String toggleDeliveryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Purchase> purchaseOpt = purchaseRepo.findById(id);
        if (purchaseOpt.isPresent()) {
            Purchase purchase = purchaseOpt.get();
            purchase.setDelivered(!purchase.isDelivered());
            purchaseRepo.save(purchase);
            redirectAttributes.addFlashAttribute("success", "Leveransstatus uppdaterad.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Belöningen hittades inte.");
        }
        return "redirect:/org-admin/dashboard";
    }

    @PostMapping("/mark-delivered/{purchaseId}")
    public String markAsDelivered(@PathVariable Long purchaseId,
                                  @AuthenticationPrincipal CustomUserDetails currentUser,
                                  RedirectAttributes redirectAttributes) {

        Optional<Purchase> purchaseOpt = purchaseRepo.findById(purchaseId);
        if (purchaseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Köpet kunde inte hittas.");
            return "redirect:/org-admin/dashboard";
        }

        Purchase purchase = purchaseOpt.get();

        User admin = currentUser.getUser();
        if (!purchase.getProduct().getCompany().getId().equals(admin.getCompany().getId())) {
            redirectAttributes.addFlashAttribute("error", "Åtkomst nekad.");
            return "redirect:/org-admin/dashboard";
        }

        purchase.setDelivered(true);
        purchaseRepo.save(purchase);
        redirectAttributes.addFlashAttribute("success", "Belöningen har markerats som levererad.");
        return "redirect:/org-admin/dashboard";
    }


}
