package com.example.coday.controller;

import com.example.coday.dto.CompanyRequest;
import com.example.coday.dto.CompanyRequest.AddressRequest;
import com.example.coday.model.Address;
import com.example.coday.model.Company;
import com.example.coday.repository.AddressRepo;
import com.example.coday.repository.CompanyRepo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyRepo companyRepo;
    private final AddressRepo addressRepo;

    public CompanyController(CompanyRepo companyRepo, AddressRepo addressRepo) {
        this.companyRepo = companyRepo;
        this.addressRepo = addressRepo;
    }

    @GetMapping("/apply")
    public String showCompanyApplicationForm(Model model, @RequestParam(value = "success", required = false) String success) {
        model.addAttribute("companyRequest", new CompanyRequest());
        model.addAttribute("success", success);
        return "apply-company";
    }

    @PostMapping("/apply")
    public String handleCompanyApplication(
            @Valid @ModelAttribute("companyRequest") CompanyRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            return "apply-company";
        }

        if (companyRepo.existsByName(request.getName())) {
            model.addAttribute("error", "Företagsnamnet är redan registrerat.");
            return "apply-company";
        }

        String cleanedOrgNumber = request.getOrgNumber().replaceAll("-", "").replaceAll("\\s+", "");

        Company company = new Company();
        company.setName(request.getName());
        company.setOrgNumber(cleanedOrgNumber);

        Set<Address> addresses = new HashSet<>();
        for (AddressRequest addr : request.getAddresses()) {
            Optional<Address> existing = addressRepo.findByStreetAddressAndPostalCodeAndCity(
                    addr.streetAddress, addr.postalCode, addr.city
            );
            addresses.add(existing.orElseGet(() -> addressRepo.save(
                    new Address(addr.streetAddress, addr.postalCode, addr.city)
            )));
        }

        company.setAddresses(addresses);
        companyRepo.save(company);

        redirectAttributes.addAttribute("success", "Din ansökan har skickats in!");
        return "redirect:/companies/apply";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<String> createCompany(@RequestBody CompanyRequest request) {
        if (companyRepo.existsByName(request.getName())) {
            return ResponseEntity.badRequest().body("Företagsnamnet är redan taget.");
        }

        String cleanedOrgNumber = request.getOrgNumber().replaceAll("-", "").replaceAll("\\s+", "");

        Company company = new Company(request.getName(), cleanedOrgNumber);

        Set<Address> addresses = new HashSet<>();
        for (AddressRequest addr : request.getAddresses()) {
            Optional<Address> existing = addressRepo.findByStreetAddressAndPostalCodeAndCity(
                    addr.streetAddress, addr.postalCode, addr.city
            );
            addresses.add(existing.orElseGet(() -> {
                Address newAddr = new Address(addr.streetAddress, addr.postalCode, addr.city);
                return addressRepo.save(newAddr);
            }));
        }

        company.setAddresses(addresses);
        companyRepo.save(company);

        return ResponseEntity.ok("Företaget har skapats med adresser.");
    }

    @GetMapping
    @ResponseBody
    public List<Company> getAllCompanies() {
        return companyRepo.findAll();
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<Company>> searchCompaniesByName(@RequestParam String name) {
        List<Company> companies = companyRepo.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(companies);
    }
}
