package com.example.coday.controller;

import com.example.coday.dto.CompanyRequest;
import com.example.coday.model.Address;
import com.example.coday.model.Company;
import com.example.coday.model.User;
import com.example.coday.repository.AddressRepo;
import com.example.coday.repository.CompanyRepo;
import com.example.coday.repository.UserRepo;
import com.example.coday.util.PostalCodeUtil;
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
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("companyRequest", new CompanyRequest());
        return "index";
    }
    @GetMapping("/register")
    public String showRegistrationForm(@RequestParam("token") String token, Model model) {
        Company company = companyRepo.findByRegistrationToken(token).orElse(null);

        if (company == null) {
            model.addAttribute("error", "Ogiltig registreringslänk.");
            return "redirect:/";
        }

        User user = new User();
        user.setCompany(company);

        model.addAttribute("user", user);
        model.addAttribute("companyName", company.getName());
        return "register";
    }


    @PostMapping("/create-user")
    public String createUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result) {

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

        user.getAddress().setPostalCode(PostalCodeUtil.formatPostalCode(user.getAddress().getPostalCode()));

        Optional<Address> existing = addressRepo.findByStreetAddressIgnoreCaseAndPostalCodeAndCityIgnoreCase(
                user.getAddress().getStreetAddress(),
                user.getAddress().getPostalCode(),
                user.getAddress().getCity()
        );

        Address finalAddress = existing.orElseGet(() -> addressRepo.save(user.getAddress()));
        user.setAddress(finalAddress);

        userRepo.save(user);

        String successMessage = "Användaren " + user.getFirstName() + " " + user.getLastName() + " har skapats.";
        return "redirect:/?successMessage=" + successMessage;
    }
}
