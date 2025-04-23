package com.example.coday.controller;

import com.example.coday.model.User;
import com.example.coday.repository.UserRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final UserRepo userRepo;

    public AdminDashboardController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/role-requests")
    public String viewRoleRequests(Model model, @RequestParam(required = false) String success) {
        List<User> requestingUsers = userRepo.findAll().stream()
                .filter(user -> user.getAdminComment() != null && !user.getAdminComment().isBlank())
                .toList();

        model.addAttribute("users", requestingUsers);
        model.addAttribute("success", success != null);
        return "role-requests";
    }

    @PostMapping("/approve-role")
    public String approveRole(@RequestParam Long userId, @RequestParam String role) {
        userRepo.findById(userId).ifPresent(user -> {
            try {
                user.setRole(User.Role.valueOf(role));
                user.setAdminComment(null);
                userRepo.save(user);
            } catch (IllegalArgumentException ignored) {
            }
        });

        return "redirect:/admin/role-requests?success";
    }
}
