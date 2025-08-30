package com.furkan.digitalWallet.controller;

import com.furkan.digitalWallet.request.AuthRequest;
import com.furkan.digitalWallet.security.JwtService;
import com.furkan.digitalWallet.entity.Customer;
import com.furkan.digitalWallet.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Customer c = customerRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(c.getUsername(), c.getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "role", c.getRole().name(), "username", c.getUsername()));
    }
}
