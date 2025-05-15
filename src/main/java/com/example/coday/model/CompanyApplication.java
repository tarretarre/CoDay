package com.example.coday.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "company_applications")
public class CompanyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Företagsnamn krävs")
    private String name;

    @NotBlank(message = "Organisationsnummer krävs")
    @Column(name = "org_number", nullable = false, unique = true)
    private String orgNumber;

    @NotBlank(message = "Kontaktperson krävs")
    private String contactName;

    @NotBlank(message = "E-post krävs")
    @Email(message = "Ogiltig e-postadress")
    private String contactEmail;

    @Pattern(regexp = "^[0-9+\\- ]{6,20}$", message = "Ogiltigt telefonnummer")
    private String contactPhone;

    @Embedded
    private EmbeddedAddress address;

    private boolean approved = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public EmbeddedAddress getAddress() {
        return address;
    }

    public void setAddress(EmbeddedAddress address) {
        this.address = address;
    }
}
