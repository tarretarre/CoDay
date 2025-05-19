package com.example.coday.service;

import com.example.coday.model.User;
import com.example.coday.repository.UserRepo;
import com.example.coday.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepo.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Användare hittades inte med e-post: " + email));
//        return new CustomUserDetails(user);
//    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Försöker ladda användare med e-post: " + email);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("Ingen användare hittades med e-post: " + email);
                    return new UsernameNotFoundException("Användare hittades inte med e-post: " + email);
                });

        System.out.println("Användare hittades: " + user.getEmail() + " | Roll: " + user.getRole());

        return new CustomUserDetails(user);
    }


}
