package com.example.multitenantsaas.config;

import com.example.multitenantsaas.security.JwtAuthenticationFilter;
import com.example.multitenantsaas.security.SubdomainTenantResolverFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SubdomainTenantResolverFilter subdomainTenantResolverFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, SubdomainTenantResolverFilter subdomainTenantResolverFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.subdomainTenantResolverFilter = subdomainTenantResolverFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/tenant/login", "/master/login").permitAll()
                        .requestMatchers("/api/tenants").hasRole("SUPERADMIN")
                        .requestMatchers("/api/auth/**", "/uploads/**", "/v2/api-docs", "/v3/api-docs", "/v3/api-docs/**",
                                "/swagger-resources", "/swagger-resources/**", "/configuration/ui",
                                "/configuration/security", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(subdomainTenantResolverFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean<SubdomainTenantResolverFilter> subdomainTenantResolverFilterRegistration(SubdomainTenantResolverFilter filter) {
        FilterRegistrationBean<SubdomainTenantResolverFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(1);
        registration.setEnabled(true);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(2);
        registration.setEnabled(true);
        return registration;
    }
}