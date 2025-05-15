package com.example.coday.controller;

import com.example.coday.dto.AddressRequest;
import com.example.coday.dto.CompanyRequest;
import com.example.coday.model.CompanyApplication;
import com.example.coday.model.EmbeddedAddress;
import com.example.coday.repository.CompanyApplicationRepo;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/companies")
public class CompanyWebController {

    private final CompanyApplicationRepo applicationRepo;

    public CompanyWebController(CompanyApplicationRepo applicationRepo) {
        this.applicationRepo = applicationRepo;
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
            application.setAddress(new EmbeddedAddress(
                    addressRequest.getStreetAddress().trim(),
                    addressRequest.getPostalCode(),
                    addressRequest.getCity().trim()
            ));
        }

        applicationRepo.save(application);
        redirectAttributes.addFlashAttribute("success", "Din ansökan har skickats!");
        return "redirect:/#join-section";
    }
}