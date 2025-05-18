package com.example.coday.repository;

import com.example.coday.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepo extends JpaRepository<Address, Long> {
    Optional<Address> findByStreetAddressIgnoreCaseAndPostalCodeAndCityIgnoreCase(String streetAddress, String postalCode, String city);

}
