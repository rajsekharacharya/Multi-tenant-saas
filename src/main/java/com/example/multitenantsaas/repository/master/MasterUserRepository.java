package com.example.multitenantsaas.repository.master;

import com.example.multitenantsaas.domain.master.MasterUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterUserRepository extends JpaRepository<MasterUser, Long> {
    Optional<MasterUser> findByUsername(String username);
}