package com.example.coday.controller;

import com.example.coday.model.*;
import com.example.coday.repository.*;
import com.example.coday.security.CustomUserDetails;
import com.example.coday.util.PostalCodeUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CompanyApplicationRepo applicationRepo;
    private final CompanyRepo companyRepo;
    private final AddressRepo addressRepo;
    private final VisitRepo visitRepo;
    private final ProductRepo productRepo;
    private final PurchaseRepo purchaseRepo;

    public AdminDashboardController(UserRepo userRepo,
                                    PasswordEncoder passwordEncoder,
                                    CompanyApplicationRepo applicationRepo,
                                    CompanyRepo companyRepo,
                                    AddressRepo addressRepo,
                                    VisitRepo visitRepo,
                                    ProductRepo productRepo, PurchaseRepo purchaseRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.applicationRepo = applicationRepo;
        this.companyRepo = companyRepo;
        this.addressRepo = addressRepo;
        this.visitRepo = visitRepo;
        this.productRepo = productRepo;
        this.purchaseRepo = purchaseRepo;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestParam(value = "success", required = false) String success,
                                     @RequestParam(value = "error", required = false) String error) {

        model.addAttribute("admin", userDetails.getUser());
        model.addAttribute("success", success);
        model.addAttribute("error", error);
        model.addAttribute("applications", applicationRepo.findAll());

        List<User> allUsers = userRepo.findByApprovedTrue();
        Map<Long, Visit> activeVisits = new HashMap<>();

        for (User user : allUsers) {
            visitRepo.findByUserAndCheckOutTimeIsNull(user).ifPresent(visit ->
                    activeVisits.put(user.getId(), visit));
        }

        Map<Long, String> checkInTimes = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        for (Map.Entry<Long, Visit> entry : activeVisits.entrySet()) {
            if (entry.getValue().getCheckInTime() != null) {
                checkInTimes.put(entry.getKey(), entry.getValue().getCheckInTime().format(formatter));
            }
        }

        List<User> roleRequests = userRepo.findAll().stream()
                .filter(user -> !user.isApproved())
                .filter(user -> user.getAdminComment() != null && !user.getAdminComment().isBlank())
                .filter(user -> user.getRole() == User.Role.USER)
                .toList();

        List<Purchase> allPurchases = purchaseRepo.findAll();

        model.addAttribute("allUsers", allUsers);
        model.addAttribute("activeVisits", activeVisits);
        model.addAttribute("checkInTimes", checkInTimes);
        model.addAttribute("roleRequests", roleRequests);
        model.addAttribute("companies", companyRepo.findAll());
        model.addAttribute("allCompanies", companyRepo.findAll());
        model.addAttribute("companyCount", companyRepo.count());
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("purchases", allPurchases);

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        model.addAttribute("baseUrl", baseUrl);

        return "admin-dashboard";
    }


    @PostMapping("/companies/approve")
    public String approveCompanyHtml(@RequestParam Long applicationId,
                                     RedirectAttributes redirectAttributes) {

        Optional<CompanyApplication> optApp = applicationRepo.findById(applicationId);
        if (optApp.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ansökan kunde inte hittas.");
            return "redirect:/admin/dashboard#admin-section";
        }

        CompanyApplication app = optApp.get();
        String cleanedOrg = app.getOrgNumber().replaceAll("\\s+", "");

        if (companyRepo.existsByOrgNumber(cleanedOrg)) {
            redirectAttributes.addFlashAttribute("error", "Företaget finns redan registrerat.");
            return "redirect:/admin/dashboard#admin-section";
        }

        Address addressFromApp = app.getAddress();
        if (addressFromApp == null) {
            redirectAttributes.addFlashAttribute("error", "Adress saknas i ansökan.");
            return "redirect:/admin/dashboard#admin-section";
        }

        String street = addressFromApp.getStreetAddress().trim();
        String postal = PostalCodeUtil.formatPostalCode(addressFromApp.getPostalCode().trim());
        String city = addressFromApp.getCity().trim();

        Address address = addressRepo
                .findByStreetAddressIgnoreCaseAndPostalCodeAndCityIgnoreCase(street, postal, city)
                .orElseGet(() -> {
                    Address newAddress = new Address();
                    newAddress.setStreetAddress(street);
                    newAddress.setPostalCode(postal);
                    newAddress.setCity(city);
                    return addressRepo.save(newAddress);
                });

        Company company = new Company();
        company.setName(app.getName());
        company.setOrgNumber(cleanedOrg);
        company.setContactName(app.getContactName());
        company.setContactEmail(app.getContactEmail());
        company.setContactPhone(app.getContactPhone());
        company.setAddress(address);

        companyRepo.save(company);
        applicationRepo.delete(app);

        redirectAttributes.addFlashAttribute("success", "Företaget har godkänts.");
        return "redirect:/admin/dashboard#admin-section";
    }


    @PostMapping("/companies/delete")
    public String deleteCompanyApplication(@RequestParam Long applicationId, RedirectAttributes redirectAttributes) {
        Optional<CompanyApplication> application = applicationRepo.findById(applicationId);

        if (application.isPresent()) {
            applicationRepo.delete(application.get());
            redirectAttributes.addFlashAttribute("success", "Företagsansökan har tagits bort.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Företagsansökan kunde inte hittas.");
        }

        return "redirect:/admin/dashboard#admin-section";
    }

    @PostMapping("/delete-company")
    public String deleteCompany(@RequestParam Long companyId, RedirectAttributes redirectAttributes) {
        try {
            if (companyRepo.existsById(companyId)) {
                companyRepo.deleteById(companyId);
                redirectAttributes.addFlashAttribute("success", "Företaget har tagits bort.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Företaget kunde inte hittas.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Det går inte att ta bort företaget eftersom användare eller historik är kopplade.");
        }
        return "redirect:/admin/dashboard#company-section";
    }

    @PostMapping("/update-user")
    public String updateUserHtml(@RequestParam Long userId,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam String role,
                                 @RequestParam(required = false) Long companyId,
                                 @RequestParam(required = false) String newPassword,
                                 RedirectAttributes redirectAttributes) {

        Optional<User> optUser = userRepo.findById(userId);
        if (optUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Användare hittades inte.");
            return "redirect:/admin/dashboard#user-status-section";
        }

        User user = optUser.get();

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim());

        try {
            user.setRole(User.Role.valueOf(role));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Ogiltig roll.");
            return "redirect:/admin/dashboard#user-status-section";
        }

        if (companyId != null) {
            companyRepo.findById(companyId).ifPresent(user::setCompany);
        }

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepo.save(user);
        redirectAttributes.addFlashAttribute("success", "Användaruppgifter har uppdaterats.");
        return "redirect:/admin/dashboard#user-status-section";
    }

    @PostMapping("/approve-role")
    public String approveRole(@RequestParam Long userId,
                              @RequestParam String role,
                              RedirectAttributes redirectAttributes) {

        userRepo.findById(userId).ifPresent(user -> {
            try {
                user.setRole(User.Role.valueOf(role));
                user.setAdminComment(null);
                user.setApproved(true);
                userRepo.save(user);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Ogiltig roll.");
            }
        });

        redirectAttributes.addFlashAttribute("success", "Rollen har uppdaterats!");
        return "redirect:/admin/dashboard#role-section";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isPresent()) {
            try {
                userRepo.deleteById(userId);
                redirectAttributes.addFlashAttribute("success", "Användaren har tagits bort.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Det går inte att ta bort användaren eftersom historik eller besök är kopplade.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Användaren kunde inte hittas.");
        }

        return "redirect:/admin/dashboard#user-status-section";
    }


    @PostMapping("/force-reset-password")
    public String forceResetPassword(@RequestParam String email,
                                     @RequestParam String newPassword,
                                     RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepo.findByEmailIgnoreCase(email.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);
            redirectAttributes.addFlashAttribute("success", "Lösenordet har återställts.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Ingen användare hittades med e-postadressen.");
        }

        return "redirect:/admin/dashboard#reset-section";
    }

    @PostMapping("/check-in")
    public String checkInUser(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isPresent() && !visitRepo.existsByUserAndCheckOutTimeIsNull(userOpt.get())) {
            Visit visit = new Visit();
            visit.setUser(userOpt.get());
            visit.setCheckInTime(java.time.LocalDateTime.now());
            visitRepo.save(visit);
            redirectAttributes.addFlashAttribute("success", "Användaren har checkats in.");
        }
        return "redirect:/admin/dashboard#user-status-section";
    }

    @PostMapping("/check-out")
    public String checkOutUser(@RequestParam Long userId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isPresent()) {
            visitRepo.findByUserAndCheckOutTimeIsNull(userOpt.get()).ifPresent(visit -> {
                visit.setCheckOutTime(java.time.LocalDateTime.now());
                visitRepo.save(visit);
            });
            redirectAttributes.addFlashAttribute("success", "Användaren har checkats ut.");
        }
        return "redirect:/admin/dashboard#user-status-section";
    }


}
