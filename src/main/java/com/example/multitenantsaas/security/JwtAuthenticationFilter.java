package com.example.multitenantsaas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String requestUri = request.getRequestURI();
        logger.info("JwtAuthenticationFilter processing request: {}", requestUri);

        if (header == null || !header.startsWith("Bearer ")) {
            logger.debug("No Bearer token found in request: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            String tenant = jwtUtil.extractTenant(token);
            List<String> roles = jwtUtil.extractRoles(token);
            String currentTenant = TenantContext.getCurrentTenant();

            logger.debug("JWT details: username={}, tenant={}, roles={}, currentTenant={}", username, tenant, roles, currentTenant);

            if (currentTenant == null) {
                logger.error("TenantContext is null for request: {}", requestUri);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Tenant context not set\"}");
                return;
            }

            // Allow master tenant for superadmin actions
            if (tenant.equals(currentTenant) || (tenant.equals("master") && currentTenant.equals("master"))) {
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(token, username, tenant)) {
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("Authenticated user: {} for tenant: {}", username, tenant);
                    } else {
                        logger.warn("Invalid JWT token for user: {} and tenant: {}", username, tenant);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Invalid JWT token\"}");
                        return;
                    }
                } else {
                    logger.warn("User already authenticated or username null for request: {}", requestUri);
                }
            } else {
                logger.warn("Tenant mismatch: JWT tenant={}, currentTenant={}", tenant, currentTenant);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Tenant mismatch: expected " + currentTenant + ", got " + tenant + "\"}");
                return;
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token for request {}: {}", requestUri, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid JWT token: " + e.getMessage() + "\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}