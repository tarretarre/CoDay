package com.example.coday.controller;

import com.example.coday.model.User;
import com.example.coday.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {
    @GetMapping("/redirect-by-role")
    public String redirectByRole(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser.getUser();

        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case ORG_ADMIN -> "redirect:/org-admin/dashboard";
            case USER -> "redirect:/user/dashboard";
        };
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?logout";
    }
}
