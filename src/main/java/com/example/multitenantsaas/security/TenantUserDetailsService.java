package com.example.multitenantsaas.security;

import com.example.multitenantsaas.domain.master.MasterUser;
import com.example.multitenantsaas.domain.tenant.User;
import com.example.multitenantsaas.repository.master.MasterUserRepository;
import com.example.multitenantsaas.repository.tenant.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MasterUserRepository masterUserRepository;

    public TenantUserDetailsService(UserRepository userRepository, MasterUserRepository masterUserRepository) {
        this.userRepository = userRepository;
        this.masterUserRepository = masterUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String tenant = TenantContext.getCurrentTenant();
        if (tenant == null) {
            throw new UsernameNotFoundException("Tenant context not set");
        }

        if ("master".equals(tenant)) {
            Optional<MasterUser> masterUser = masterUserRepository.findByUsername(username);
            if (masterUser.isPresent()) {
                MasterUser mu = masterUser.get();
                return new TenantUserDetails(mu.getUsername(), mu.getPassword(), mu.getRole(), tenant);
            }
        } else {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                User u = user.get();
                return new TenantUserDetails(u.getUsername(), u.getPassword(), u.getRole(), tenant);
            }
        }

        throw new UsernameNotFoundException("User not found: " + username + " in tenant: " + tenant);
    }
}