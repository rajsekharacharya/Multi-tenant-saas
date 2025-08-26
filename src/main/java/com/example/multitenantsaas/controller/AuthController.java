package com.example.multitenantsaas.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.multitenantsaas.security.JwtUtil;
import com.example.multitenantsaas.security.TenantContext;
import com.example.multitenantsaas.security.TenantUserDetails;

import lombok.Data;

@RestController
public class AuthController {

    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserDetailsService userDetailsService, AuthenticationManager authenticationManager,
            JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/tenant/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest credentials) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        String tenant = TenantContext.getCurrentTenant();

        if (tenant == null) {
            return ResponseEntity.status(400).body("{\"error\": \"No tenant specified\"}");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            TenantUserDetails userDetails = (TenantUserDetails) userDetailsService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(username, tenant, List.of(userDetails.getRole()));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("{\"error\": \"Invalid credentials\"}");
        }
    }

    @PostMapping("/master/login")
    public ResponseEntity<?> masterLogin(@RequestBody AuthenticationRequest credentials) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            TenantUserDetails userDetails = (TenantUserDetails) userDetailsService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(username, "master", List.of(userDetails.getRole()));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("{\"error\": \"Invalid credentials\"}");
        }
    }

    @Data
    static class AuthenticationRequest {
        private String username;
        private String password;
    }

}