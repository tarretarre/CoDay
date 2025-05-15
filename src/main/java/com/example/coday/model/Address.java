package com.example.coday.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.example.coday.util.PostalCodeUtil;

import java.util.List;

@Entity
@Table(
        name = "addresses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"street_address", "postal_code", "city"})
)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Gatuadress krävs")
    @Column(name = "street_address")
    private String streetAddress;

    @NotBlank(message = "Postnummer krävs")
    @Column(name = "postal_code")
    private String postalCode;

    @NotBlank(message = "Postort krävs")
    private String city;

    @OneToMany(mappedBy = "address")
    private List<User> users;

    @OneToMany(mappedBy = "address")
    private List<Company> companies;

    public Address() {}

    public Address(String streetAddress, String postalCode, String city) {
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = PostalCodeUtil.formatPostalCode(postalCode);
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }
}
