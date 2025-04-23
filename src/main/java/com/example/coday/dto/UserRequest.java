package com.example.coday.dto;

public class UserRequest {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String ssn;
    public String adminComment;
    public Long companyId;

    public AddressRequest address;
    public static class AddressRequest {
        public String streetAddress;
        public String postalCode;
        public String city;
    }
}
