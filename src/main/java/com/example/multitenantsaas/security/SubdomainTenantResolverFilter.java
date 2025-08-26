package com.example.multitenantsaas.security;

import com.example.multitenantsaas.domain.master.Tenant;
import com.example.multitenantsaas.repository.master.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class SubdomainTenantResolverFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SubdomainTenantResolverFilter.class);

    private final TenantRepository tenantRepository;

    public SubdomainTenantResolverFilter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String host = request.getServerName();
        String requestUri = request.getRequestURI();
        logger.info("SubdomainTenantResolverFilter processing request - Host: {}, URI: {}", host, requestUri);

        // Allow main domain (e.g., localhost:8081) for master endpoints
        if (!host.contains(".")) {
            logger.info("No subdomain in Host: {}, setting TenantContext to 'master'", host);
            TenantContext.setCurrentTenant("master");
            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
                logger.debug("Cleared TenantContext for master");
            }
            return;
        }

        String subdomain = extractSubdomain(host);
        if (subdomain == null) {
            logger.error("No subdomain extracted from Host: {}", host);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No subdomain provided in Host header\"}");
            return;
        }

        subdomain = subdomain.toLowerCase();
        logger.info("Extracted subdomain: {}", subdomain);

        try {
            Optional<Tenant> tenant = tenantRepository.findBySubdomain(subdomain);
            if (tenant.isPresent()) {
                logger.info("Tenant found for subdomain: {}, tenant: {}", subdomain, tenant.get());
                TenantContext.setCurrentTenant(subdomain);
            } else {
                logger.error("No tenant found in database for subdomain: {}", subdomain);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid tenant: " + subdomain + "\"}");
                return;
            }
        } catch (Exception e) {
            logger.error("Error querying tenant for subdomain: {}, error: {}", subdomain, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Error querying tenant: " + e.getMessage() + "\"}");
            return;
        }

        try {
            logger.debug("Proceeding with filter chain, TenantContext: {}", TenantContext.getCurrentTenant());
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            logger.debug("Cleared TenantContext for subdomain: {}", subdomain);
        }
    }

    private String extractSubdomain(String host) {
        if (host == null || !host.contains(".")) {
            logger.warn("Invalid Host header, no subdomain found: {}", host);
            return null;
        }
        String subdomain = host.split("\\.")[0];
        logger.debug("Extracted subdomain from host {}: {}", host, subdomain);
        return subdomain;
    }
}