package com.example.coday.controller;

import com.example.coday.model.User;
import com.example.coday.model.User.Role;
import com.example.coday.repository.UserRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepo userRepo;

    public AdminUserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public String viewPendingUsers(Model model) {
        List<User> usersWithComment = userRepo.findByAdminCommentIsNotNull();
        model.addAttribute("users", usersWithComment);
        return "admin-user-review";
    }

    @PostMapping("/{id}/set-role")
    public String updateUserRole(@PathVariable Long id, @RequestParam String role) {
        userRepo.findById(id).ifPresent(user -> {
            user.setRole(Role.valueOf(role));
            userRepo.save(user);
        });
        return "redirect:/admin/users";
    }
}
