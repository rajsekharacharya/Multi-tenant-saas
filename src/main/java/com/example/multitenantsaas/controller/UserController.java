package com.example.multitenantsaas.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.multitenantsaas.domain.master.MasterUser;
import com.example.multitenantsaas.domain.tenant.User;
import com.example.multitenantsaas.repository.master.MasterUserRepository;
import com.example.multitenantsaas.repository.tenant.UserRepository;

import jakarta.validation.Valid;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final MasterUserRepository masterUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            MasterUserRepository masterUserRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.masterUserRepository = masterUserRepository;
    }

    @PostMapping("/tenant/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/tenant/users")
    public ResponseEntity<List<User>> getUser() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/master/users")
    public ResponseEntity<MasterUser> createUserForMaster(@Valid @RequestBody MasterUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(masterUserRepository.save(user));
    }

    @GetMapping("/master/users")
    public ResponseEntity<List<MasterUser>> getUserForMaster() {
        return ResponseEntity.ok(masterUserRepository.findAll());
    }
}