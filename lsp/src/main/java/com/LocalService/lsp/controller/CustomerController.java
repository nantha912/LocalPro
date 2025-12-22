package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Customer;
import com.LocalService.lsp.model.LoginRequest;
import com.LocalService.lsp.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialNotFoundException;
import java.nio.file.attribute.UserPrincipalNotFoundException;

// Note: Using a DTO (Data Transfer Object) for the request body is a best practice.
// For simplicity, this example uses the Customer model directly.

@RestController
@RequestMapping("/api/auth") // A common base path for authentication-related endpoints
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        logger.info("Request received to register customer with email: {}", customer.getEmail());
        try {
            Customer registeredCustomer = customerService.registerCustomer(customer);
            // Return the created customer (without the password) and a 201 Created status
            registeredCustomer.setPassword(null); // Avoid sending the hashed password back
            logger.info("Customer registered successfully with ID: {}", registeredCustomer.getId());
            return new ResponseEntity<>(registeredCustomer, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            logger.warn("Registration failed: Email already in use for email: {}", customer.getEmail());
            // Return an error response if the email is already in use
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during registration for email: {}", customer.getEmail(), e);
            // Catch any other potential errors
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {
        logger.info("Request received to login customer with email: {}", loginRequest.getEmail());
        try {
            Customer customer = customerService.loginCustomer(loginRequest.getEmail(), loginRequest.getPassword());
            customer.setPassword(null); // Never send the password back to the client
            logger.info("Customer logged in successfully: {}", customer.getEmail());
            return ResponseEntity.ok(customer);
        } catch (UserPrincipalNotFoundException e) {
            logger.warn("Login failed: User not found for email: {}", loginRequest.getEmail());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404
        } catch (CredentialNotFoundException e) {
            logger.warn("Login failed: Invalid credentials for email: {}", loginRequest.getEmail());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED); // 401
        } catch (Exception e) {
            logger.error("An unexpected error occurred during login for email: {}", loginRequest.getEmail(), e);
            return new ResponseEntity<>("An unexpected error occurred during login.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check request received.");
        return ResponseEntity.ok("Healthy");
    }
}