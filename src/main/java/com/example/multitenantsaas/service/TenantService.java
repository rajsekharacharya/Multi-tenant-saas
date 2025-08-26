package com.example.multitenantsaas.service;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.multitenantsaas.config.TenantRoutingDataSource;
import com.example.multitenantsaas.domain.master.Tenant;
import com.example.multitenantsaas.repository.master.TenantRepository;

import jakarta.validation.Valid;

@Service
@Validated
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final DataSource masterDataSource;
    private final TenantRoutingDataSource tenantRoutingDataSource;

    public TenantService(TenantRepository tenantRepository, DataSource masterDataSource, TenantRoutingDataSource tenantRoutingDataSource) {
        this.tenantRepository = tenantRepository;
        this.masterDataSource = masterDataSource;
        this.tenantRoutingDataSource = tenantRoutingDataSource;
    }

    public Tenant createTenant(@Valid Tenant tenant) {
        String dbName = tenant.getSubdomain().toLowerCase();
        logger.info("Creating tenant with subdomain: {}", dbName);
        tenant.setSubdomain(dbName);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(masterDataSource);
        jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "`");

        tenant.setDbUrl("jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true");
        tenant.setDbUsername("root");
        tenant.setDbPassword("root");

        logger.debug("Saving tenant to master database: {}", tenant);
        Tenant savedTenant = tenantRepository.save(tenant);

        // Add tenant datasource to routing datasource
        tenantRoutingDataSource.addTenantDataSource(savedTenant);

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(savedTenant.getDbUrl());
        ds.setUsername(savedTenant.getDbUsername());
        ds.setPassword(savedTenant.getDbPassword());

        logger.info("Running Flyway migrations for tenant: {}", dbName);
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration/tenant")
                .load();
        flyway.migrate();

        logger.info("Tenant created successfully: {}", dbName);
        return savedTenant;
    }
}