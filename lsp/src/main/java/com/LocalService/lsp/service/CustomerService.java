package com.LocalService.lsp.service;

import com.LocalService.lsp.model.Customer;
import com.LocalService.lsp.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialNotFoundException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds a customer by their unique ID.
     */
    public Optional<Customer> getCustomerById(String id) {
        logger.info("Service: Fetching customer by ID: {}", id);
        return customerRepository.findById(id);
    }

    /**
     * Checks if an email is already registered in the system.
     * Required for frontend pre-registration validation.
     */
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    /**
     * Registers a new customer.
     */
    public Customer registerCustomer(Customer customer) {
        logger.info("Attempting to register customer with email: {}", customer.getEmail());

        if (customerRepository.existsByEmail(customer.getEmail())) {
            logger.warn("Registration failed: Email {} is already in use.", customer.getEmail());
            throw new IllegalStateException("Error: Email is already in use!");
        }

        String encodedPassword = passwordEncoder.encode(customer.getPassword());
        customer.setPassword(encodedPassword);

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer registered successfully with ID: {}", savedCustomer.getId());

        return savedCustomer;
    }

    /**
     * Authenticates a customer.
     */
    public Customer loginCustomer(String email, String password) throws UserPrincipalNotFoundException, CredentialNotFoundException {
        logger.info("Attempting to login customer with email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found for email: {}", email);
                    return new UserPrincipalNotFoundException("User does not exist.");
                });

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            logger.warn("Login failed: Wrong password for email: {}", email);
            throw new CredentialNotFoundException("Wrong password.");
        }

        logger.info("Customer logged in successfully: {}", email);
        return customer;
    }
}