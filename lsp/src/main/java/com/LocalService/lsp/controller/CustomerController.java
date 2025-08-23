package com.LocalService.lsp.controller;

import com.LocalService.lsp.model.Customer;
import com.LocalService.lsp.model.LoginRequest;
import com.LocalService.lsp.service.CustomerService;
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

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        try {
            Customer registeredCustomer = customerService.registerCustomer(customer);
            // Return the created customer (without the password) and a 201 Created status
            registeredCustomer.setPassword(null); // Avoid sending the hashed password back
            return new ResponseEntity<>(registeredCustomer, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            // Return an error response if the email is already in use
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            // Catch any other potential errors
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {
        try {
            Customer customer = customerService.loginCustomer(loginRequest.getEmail(), loginRequest.getPassword());
            customer.setPassword(null); // Never send the password back to the client
            return ResponseEntity.ok(customer);
        } catch (UserPrincipalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404
        } catch (CredentialNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED); // 401
        } catch (Exception e) {
            return new ResponseEntity<>("An unexpected error occurred during login.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

