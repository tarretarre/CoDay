package com.example.coday.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import com.example.coday.util.PostalCodeUtil;


@Embeddable
public class EmbeddedAddress {

    @NotBlank(message = "Gatuadress krävs")
    private String streetAddress;

    @NotBlank(message = "Postnummer krävs")
    private String postalCode;

    @NotBlank(message = "Postort krävs")
    private String city;

    public EmbeddedAddress() {
    }

    public EmbeddedAddress(String streetAddress, String postalCode, String city) {
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.city = city;
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
}
