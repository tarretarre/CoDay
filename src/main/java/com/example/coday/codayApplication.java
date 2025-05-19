package com.example.coday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class codayApplication {

	public static void main(String[] args) {
		SpringApplication.run(codayApplication.class, args);
	}
}
