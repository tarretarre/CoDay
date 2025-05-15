package com.example.coday.dto;

import com.example.coday.util.PostalCodeUtil;
import jakarta.validation.constraints.NotBlank;

public class AddressRequest {

    @NotBlank(message = "Gatuadress krävs")
    private String streetAddress;

    @NotBlank(message = "Postnummer krävs")
    private String postalCode;

    @NotBlank(message = "Postort krävs")
    private String city;

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