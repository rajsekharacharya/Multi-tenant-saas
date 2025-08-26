// package com.example.multitenantsaas.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;

// @Configuration
// public class SecurityConfiguration {

//     @Bean
//     public UserDetailsService userDetailsService() {
//         // Return a dummy UserDetailsService to disable inMemoryUserDetailsManager
//         return username -> {
//             throw new UsernameNotFoundException("No in-memory users configured");
//         };
//     }
// }