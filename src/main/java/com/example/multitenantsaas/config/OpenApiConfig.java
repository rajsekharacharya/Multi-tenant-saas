package com.example.multitenantsaas.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Multi-Tenant SaaS API",
        version = "1.0.0",
        description = "API documentation for the Multi-Tenant SaaS platform. This project provides a scalable, secure, and robust backend for managing multiple tenants within a single database architecture. Features include tenant isolation, user authentication, role-based access control, and resource management.",
        contact = @Contact(
            name = "Rajsekhar Acharya",
            email = "Rajsekhar Acharya"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    externalDocs = @ExternalDocumentation(
        description = "Multi-Tenant SaaS Project GitHub Repository",
        url = "https://github.com"
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "JWT Bearer Token Authentication. Obtain token via /auth/login endpoint.")
public class OpenApiConfig {
}