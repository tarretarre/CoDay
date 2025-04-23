package com.example.coday.dto;

public class UserResponse {
    public Long id;
    public String firstName;
    public String lastName;
    public String email;
    public String ssn;
    public int points;
    public String role;
    public String adminComment;

    public CompanyDto company;
    public AddressDto address;

    public static class CompanyDto {
        public Long id;
        public String name;
        public String orgNumber;
    }

    public static class AddressDto {
        public Long id;
        public String streetAddress;
        public String postalCode;
        public String city;
    }
}
