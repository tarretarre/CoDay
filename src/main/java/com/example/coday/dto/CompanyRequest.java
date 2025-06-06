package com.example.coday.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CompanyRequest {

    @NotBlank(message = "Företagsnamn krävs")
    private String name;

    @NotBlank(message = "Organisationsnummer krävs")
    @Size(min = 10, max = 10, message = "Organisationsnumret ska vara exakt 10 tecken")
    private String orgNumber;

    private List<AddressRequest> addresses;

    @NotBlank(message = "Kontaktperson krävs")
    private String contactName;

    @NotBlank(message = "E-post krävs")
    @Email(message = "Ogiltig e-postadress")
    private String contactEmail;

    @Pattern(regexp = "^[0-9+\\- ]{6,20}$", message = "Ogiltigt telefonnummer")
    private String contactPhone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public List<AddressRequest> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressRequest> addresses) {
        this.addresses = addresses;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
