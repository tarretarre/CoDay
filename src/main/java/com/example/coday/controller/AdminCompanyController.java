package com.example.coday.controller;

import com.example.coday.dto.CompanyRequest;
import com.example.coday.model.Address;
import com.example.coday.model.Company;
import com.example.coday.repository.AddressRepo;
import com.example.coday.repository.CompanyRepo;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/companies")
public class AdminCompanyController {

    private final CompanyRepo companyRepo;
    private final AddressRepo addressRepo;

    public AdminCompanyController(CompanyRepo companyRepo, AddressRepo addressRepo) {
        this.companyRepo = companyRepo;
        this.addressRepo = addressRepo;
    }

    @GetMapping
    public String showForm(Model model,
                           @RequestParam(value = "success", required = false) String successMessage) {
        model.addAttribute("companyRequest", new CompanyRequest());
        model.addAttribute("success", successMessage);
        return "register-company";
    }

    @PostMapping
    public String createCompany(@Valid @ModelAttribute("companyRequest") CompanyRequest request,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (result.hasErrors()) {
            return "register-company";
        }

        if (companyRepo.existsByName(request.getName())) {
            model.addAttribute("error", "Företagsnamnet är redan taget.");
            return "register-company";
        }

        String cleanedOrgNumber = request.getOrgNumber().replaceAll("-", "").trim();

        Company company = new Company();
        company.setName(request.getName());
        company.setOrgNumber(cleanedOrgNumber);

        Set<Address> addresses = new HashSet<>();

        for (CompanyRequest.AddressRequest addr : request.getAddresses()) {
            Optional<Address> existing = addressRepo.findByStreetAddressAndPostalCodeAndCity(
                    addr.streetAddress, addr.postalCode, addr.city
            );
            addresses.add(existing.orElseGet(() -> addressRepo.save(
                    new Address(addr.streetAddress, addr.postalCode, addr.city))));
        }

        company.setAddresses(addresses);
        companyRepo.save(company);

        redirectAttributes.addAttribute("success", "Företaget \"" + request.getName() + "\" har registrerats.");
        return "redirect:/admin/companies";
    }

}
