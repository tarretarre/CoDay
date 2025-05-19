package com.example.coday.controller;

import com.example.coday.dto.AddressRequest;
import com.example.coday.dto.CompanyRequest;
import com.example.coday.model.CompanyApplication;
import com.example.coday.model.Address;
import com.example.coday.repository.AddressRepo;
import com.example.coday.repository.CompanyApplicationRepo;
import com.example.coday.util.PostalCodeUtil;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/companies")
public class CompanyWebController {

    private final CompanyApplicationRepo applicationRepo;
    private final AddressRepo addressRepo;

    public CompanyWebController(CompanyApplicationRepo applicationRepo, AddressRepo addressRepo) {
        this.applicationRepo = applicationRepo;
        this.addressRepo = addressRepo;
    }

    @PostMapping("/apply")
    public String handleCompanyApplication(@ModelAttribute("companyRequest") @Valid CompanyRequest companyRequest,
                                           BindingResult bindingResult,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("companyRequest", companyRequest);
            return "index";
        }

        String cleanedOrgNumber = companyRequest.getOrgNumber().replaceAll("\\s+", "").trim();

        if (applicationRepo.existsByOrgNumber(cleanedOrgNumber)) {
            redirectAttributes.addFlashAttribute("error", "Det finns redan en ansökan med detta organisationsnummer.");
            return "redirect:/#join-section";
        }

        CompanyApplication application = new CompanyApplication();
        application.setName(companyRequest.getName());
        application.setOrgNumber(cleanedOrgNumber);
        application.setContactName(companyRequest.getContactName());
        application.setContactEmail(companyRequest.getContactEmail());
        application.setContactPhone(companyRequest.getContactPhone());

        if (companyRequest.getAddresses() != null && !companyRequest.getAddresses().isEmpty()) {
            AddressRequest addressRequest = companyRequest.getAddresses().get(0);

            String street = addressRequest.getStreetAddress().trim();
            String postal = PostalCodeUtil.formatPostalCode(addressRequest.getPostalCode().trim());
            String city = addressRequest.getCity().trim();

            Optional<Address> existingAddress = addressRepo
                    .findByStreetAddressIgnoreCaseAndPostalCodeAndCityIgnoreCase(street, postal, city);

            Address addressToUse;

            if (existingAddress.isPresent()) {
                addressToUse = existingAddress.get();
            } else {
                Address newAddress = new Address();
                newAddress.setStreetAddress(street);
                newAddress.setPostalCode(postal);
                newAddress.setCity(city);
                addressToUse = addressRepo.save(newAddress);
            }

            application.setAddress(addressToUse);
        }


        applicationRepo.save(application);
        redirectAttributes.addFlashAttribute("success", "Din ansökan har skickats!");
        return "redirect:/#join-section";
    }
}