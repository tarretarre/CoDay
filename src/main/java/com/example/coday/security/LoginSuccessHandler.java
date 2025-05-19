package com.example.coday.security;

import com.example.coday.model.User;
import com.example.coday.repository.UserRepo;
import jakarta.servlet.ServletException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepo userRepo;

    public LoginSuccessHandler(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        user.setLastLogin(LocalDateTime.now());
        userRepo.save(user);

        String redirectURL = switch (user.getRole().name()) {
            case "ADMIN" -> "/admin/dashboard";
            case "ORG_ADMIN" -> "/org-admin/dashboard";
            case "USER" -> "/user/dashboard";
            default -> "/";
        };

        response.sendRedirect(redirectURL);
    }

}
