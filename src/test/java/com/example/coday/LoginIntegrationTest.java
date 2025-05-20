package com.example.coday;

import com.example.coday.model.Address;
import com.example.coday.model.Company;
import com.example.coday.model.User;
import com.example.coday.repository.AddressRepo;
import com.example.coday.repository.CompanyRepo;
import com.example.coday.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        String email = "login@example.com";
        if (userRepo.findByEmail(email).isEmpty()) {
            Address address = new Address();
            address.setStreetAddress("Testis 1");
            address.setPostalCode("123 45");
            address.setCity("Teststad");
            addressRepo.save(address);

            Company company = new Company();
            company.setName("Testbolag AB");
            company.setOrgNumber("1234567890");
            company.setAddress(address);
            company.setContactEmail("contact@example.com");
            company.setContactName("Test Contact");
            company.setContactPhone("12345678");
            companyRepo.save(company);

            User user = new User();
            user.setFirstName("Login");
            user.setLastName("Test");
            user.setEmail(email);
            user.setSsn("199001019999");
            user.setPassword(passwordEncoder.encode("securePassword"));
            user.setCompany(company);
            user.setAddress(address);
            user.setApproved(true);
            userRepo.save(user);
        }
    }

    @Test
    public void loginWithValidCredentialsShouldSucceed() throws Exception {
        mockMvc.perform(formLogin()
                        .user("email", "login@example.com")
                        .password("password", "securePassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));
    }

    @Test
    public void loginWithInvalidPasswordShouldFail() throws Exception {
        mockMvc.perform(formLogin()
                        .user("email", "testEmail")
                        .password("password", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?error"));
    }

    @Test
    public void loginWithUnapprovedUserShouldFail() throws Exception {
        String unapprovedEmail = "notapproved@example.com";
        if (userRepo.findByEmail(unapprovedEmail).isEmpty()) {
            Address address = new Address();
            address.setStreetAddress("Unapproved Gata " + System.currentTimeMillis());
            address.setPostalCode("123 45");
            address.setCity("Unapprovedstad");
            addressRepo.save(address);

            Company company = new Company();
            company.setName("EjGodkäntBolag AB " + System.currentTimeMillis());
            company.setOrgNumber("987654" + (int)(Math.random() * 10000));
            company.setAddress(address);
            company.setContactEmail("nope@example.com");
            company.setContactName("Ingen");
            company.setContactPhone("000000");
            companyRepo.save(company);

            User user = new User();
            user.setFirstName("Ej");
            user.setLastName("Godkänd");
            user.setEmail(unapprovedEmail);
            user.setSsn("199001019998");
            user.setPassword(passwordEncoder.encode("securePassword"));
            user.setCompany(company);
            user.setAddress(address);
            user.setApproved(false); // <- 👈 detta är nyckeln!
            userRepo.save(user);
        }

        mockMvc.perform(formLogin()
                        .user("email", unapprovedEmail)
                        .password("password", "securePassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));
    }

    @Test
    public void userShouldRedirectToUserDashboardAfterLogin() throws Exception {
        String userEmail = "user@example.com";
        if (userRepo.findByEmail(userEmail).isEmpty()) {
            Address address = createUniqueAddress();
            Company company = createCompany(address);

            User user = new User();
            user.setFirstName("User");
            user.setLastName("Test");
            user.setEmail(userEmail);
            user.setSsn("199001011234");
            user.setPassword(passwordEncoder.encode("securePassword"));
            user.setCompany(company);
            user.setAddress(address);
            user.setApproved(true);
            user.setRole(User.Role.USER); // 👈 USER
            userRepo.save(user);
        }

        mockMvc.perform(formLogin()
                        .user("email", userEmail)
                        .password("password", "securePassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));
    }

    @Test
    public void orgAdminShouldRedirectToOrgAdminDashboardAfterLogin() throws Exception {
        String adminEmail = "orgadmin@example.com";
        if (userRepo.findByEmail(adminEmail).isEmpty()) {
            Address address = createUniqueAddress();
            Company company = createCompany(address);

            User user = new User();
            user.setFirstName("Org");
            user.setLastName("Admin");
            user.setEmail(adminEmail);
            user.setSsn("199001017777");
            user.setPassword(passwordEncoder.encode("securePassword"));
            user.setCompany(company);
            user.setAddress(address);
            user.setApproved(true);
            user.setRole(User.Role.ORG_ADMIN); // 👈 ORG_ADMIN
            userRepo.save(user);
        }

        mockMvc.perform(formLogin()
                        .user("email", adminEmail)
                        .password("password", "securePassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/redirect-by-role"));
    }

    private Address createUniqueAddress() {
        Address address = new Address();
        address.setStreetAddress("Testgatan " + System.nanoTime());
        address.setPostalCode("123 45");
        address.setCity("Teststad");
        return addressRepo.save(address);
    }

    private Company createCompany(Address address) {
        Company company = new Company();
        company.setName("Testföretag " + System.nanoTime());
        company.setOrgNumber("999999" + (int)(Math.random() * 10000));
        company.setAddress(address);
        company.setContactEmail("kontakt@test.se");
        company.setContactName("Kontaktperson");
        company.setContactPhone("0701234567");
        return companyRepo.save(company);
    }

}
