package com.LocalService.lsp.service;

import com.LocalService.lsp.model.Customer;
import com.LocalService.lsp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialNotFoundException;
import java.nio.file.attribute.UserPrincipalNotFoundException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new customer.
     * @param customer The customer object containing name, email, and plain-text password.
     * @return The saved customer object.
     * @throws IllegalStateException if a customer with the same email already exists.
     */
    public Customer registerCustomer(Customer customer) {
        // Check if a customer with the email already exists
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalStateException("Error: Email is already in use!");
        }

        // Encode the password before saving
        String encodedPassword = passwordEncoder.encode(customer.getPassword());
        customer.setPassword(encodedPassword);

        // Save the new customer to the database
        return customerRepository.save(customer);
    }
    /**
     * Authenticates a customer.
     * @param email The customer's email.
     * @param password The customer's plain-text password.
     * @return The authenticated customer object.
     * @throws UserPrincipalNotFoundException if no user is found with the given email.
     * @throws CredentialNotFoundException if the password does not match.
     */
    public Customer loginCustomer(String email, String password) throws UserPrincipalNotFoundException, CredentialNotFoundException {
        // Find the customer by email or throw an exception if not found
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UserPrincipalNotFoundException("User does not exist."));

        // Check if the provided password matches the stored hashed password
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new CredentialNotFoundException("Wrong password.");
        }

        return customer;
    }
}
