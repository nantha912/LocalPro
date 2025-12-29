package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Customer;
import com.LocalService.lsp.model.LoginRequest;
import com.LocalService.lsp.service.CustomerService;
import com.LocalService.lsp.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialNotFoundException;
import java.nio.file.attribute.UserPrincipalNotFoundException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Critical for frontend access
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * FIX: Added missing endpoint to fetch customer by ID.
     * This resolves the "Profile Unavailable" issue when clicking names on payment cards.
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable String id) {
        logger.info("Fetching public profile for Customer ID: {}", id);
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setPassword(null); // Security: Hide hashed password
                    return ResponseEntity.ok(customer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        try {
            Customer registeredCustomer = customerService.registerCustomer(customer);
            registeredCustomer.setPassword(null);
            return new ResponseEntity<>(registeredCustomer, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            Customer customer = customerService.loginCustomer(loginRequest.getEmail(), loginRequest.getPassword());
            customer.setPassword(null);
            return ResponseEntity.ok(customer);
        } catch (UserPrincipalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (CredentialNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred during login.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Healthy");
    }
}