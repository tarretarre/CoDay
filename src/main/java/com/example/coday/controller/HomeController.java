package com.example.coday.controller;

import com.example.coday.model.Address;
import com.example.coday.model.Company;
import com.example.coday.model.User;
import com.example.coday.repository.AddressRepo;
import com.example.coday.repository.CompanyRepo;
import com.example.coday.repository.UserRepo;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;
    private final AddressRepo addressRepo;
    private final PasswordEncoder passwordEncoder;

    public HomeController(UserRepo userRepo,
                          CompanyRepo companyRepo,
                          AddressRepo addressRepo,
                          PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.companyRepo = companyRepo;
        this.addressRepo = addressRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam(required = false) String successMessage) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("successMessage", successMessage); // Kan vara null
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(@RequestParam(required = false) Long companyId, Model model) {
        User user = new User();
        user.setAddress(new Address());

        if (companyId != null) {
            Company company = companyRepo.findById(companyId).orElse(null);
            user.setCompany(company);
        }

        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/create-user")
    public String createUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             Model model) {

        if (user.getAddress() == null) {
            user.setAddress(new Address());
        }

        if (result.hasErrors()) {
            return "register";
        }

        String ssn = user.getSsn().replaceAll("-", "").trim();
        if (ssn.length() == 12) {
            ssn = ssn.substring(2);
        }
        user.setSsn(ssn);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Optional<Address> existing = addressRepo.findByStreetAddressAndPostalCodeAndCity(
                user.getAddress().getStreetAddress(),
                user.getAddress().getPostalCode(),
                user.getAddress().getCity()
        );

        if (existing.isPresent()) {
            user.setAddress(existing.get());
        } else {
            addressRepo.save(user.getAddress());
        }

        userRepo.save(user);

        return "redirect:/?successMessage=Användaren " + user.getFirstName() + " " + user.getLastName() + " har skapats.";
    }
}
