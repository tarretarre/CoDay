package com.example.coday.controller;

import com.example.coday.dto.UserRequest;
import com.example.coday.dto.UserResponse;
import com.example.coday.model.Address;
import com.example.coday.model.Company;
import com.example.coday.model.User;
import com.example.coday.repository.AddressRepo;
import com.example.coday.repository.CompanyRepo;
import com.example.coday.repository.PurchaseRepo;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.VisitRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepo userRepo;
    private final VisitRepo visitRepo;
    private final PurchaseRepo purchaseRepo;
    private final CompanyRepo companyRepo;
    private final AddressRepo addressRepo;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepo userRepo,
                          VisitRepo visitRepo,
                          PurchaseRepo purchaseRepo,
                          CompanyRepo companyRepo,
                          AddressRepo addressRepo,
                          PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.visitRepo = visitRepo;
        this.purchaseRepo = purchaseRepo;
        this.companyRepo = companyRepo;
        this.addressRepo = addressRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRequest request) {
        if (request.firstName == null || request.firstName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Förnamn krävs.");
        }

        if (request.lastName == null || request.lastName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Efternamn krävs.");
        }

        if (request.email == null || request.email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("E-postadress krävs.");
        }

        if (userRepo.findByEmail(request.email).isPresent()) {
            return ResponseEntity.badRequest().body("En användare med denna e-post finns redan.");
        }

        if (request.password == null || request.password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Lösenord krävs.");
        }

        if (request.ssn == null) {
            return ResponseEntity.badRequest().body("Personnummer krävs.");
        }

        String rawSsn = request.ssn.replaceAll("[-\\s]", "");

        if (!rawSsn.matches("\\d{10}") && !rawSsn.matches("\\d{12}")) {
            return ResponseEntity.badRequest().body("Personnummer måste vara 10 eller 12 siffror.");
        }

        String sanitizedSsn = rawSsn.length() == 12 ? rawSsn.substring(2) : rawSsn;

        if (userRepo.findBySsn(sanitizedSsn).isPresent()) {
            return ResponseEntity.badRequest().body("En användare med detta personnummer finns redan.");
        }

        Address address = addressRepo.findByStreetAddressAndPostalCodeAndCity(
                request.address.streetAddress,
                request.address.postalCode,
                request.address.city
        ).orElseGet(() -> addressRepo.save(new Address(
                request.address.streetAddress,
                request.address.postalCode,
                request.address.city
        )));

        Company company = null;
        if (request.companyId != null) {
            company = companyRepo.findById(request.companyId).orElse(null);
        }

        User user = new User();
        user.setFirstName(request.firstName);
        user.setLastName(request.lastName);
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setSsn(sanitizedSsn); // Alltid 10 siffror
        user.setCompany(company);
        user.setAddress(address);
        user.setAdminComment(request.adminComment);
        user.setRole(User.Role.USER);

        userRepo.save(user);
        return ResponseEntity.ok("Användaren har registrerats.");
    }


    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream().map(this::mapToResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        if (visitRepo.existsByUser(user) || purchaseRepo.existsByUser(user) || user.getPoints() > 0) {
            return ResponseEntity.badRequest().body("Kan inte ta bort användaren eftersom den har historik eller poäng.");
        }

        userRepo.delete(user);
        return ResponseEntity.ok("Användaren har tagits bort.");
    }

    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();
        res.id = user.getId();
        res.firstName = user.getFirstName();
        res.lastName = user.getLastName();
        res.email = user.getEmail();
        res.ssn = user.getSsn().length() > 10 ? user.getSsn().substring(user.getSsn().length() - 10) : user.getSsn();
        res.points = user.getPoints();
        res.adminComment = user.getAdminComment();
        res.role = user.getRole().name();

        if (user.getCompany() != null) {
            UserResponse.CompanyDto c = new UserResponse.CompanyDto();
            c.id = user.getCompany().getId();
            c.name = user.getCompany().getName();
            c.orgNumber = user.getCompany().getOrgNumber();
            res.company = c;
        }

        if (user.getAddress() != null) {
            UserResponse.AddressDto a = new UserResponse.AddressDto();
            a.id = user.getAddress().getId();
            a.streetAddress = user.getAddress().getStreetAddress();
            a.postalCode = user.getAddress().getPostalCode();
            a.city = user.getAddress().getCity();
            res.address = a;
        }

        return res;
    }

    @PutMapping("/{userId}/role")
    @ResponseBody
    public ResponseEntity<String> updateUserRoleViaApi(@PathVariable Long userId,
                                                       @RequestParam String newRole) {
        return userRepo.findById(userId)
                .map(user -> {
                    try {
                        user.setRole(User.Role.valueOf(newRole));
                        userRepo.save(user);
                        return ResponseEntity.ok("Roll uppdaterad till " + newRole);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Ogiltig roll: " + newRole);
                    }
                })
                .orElse(ResponseEntity.badRequest().body("Användare hittades inte."));
    }

}
