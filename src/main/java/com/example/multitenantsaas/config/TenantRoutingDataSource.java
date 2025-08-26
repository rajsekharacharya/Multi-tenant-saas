package com.example.multitenantsaas.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.example.multitenantsaas.domain.master.Tenant;
import com.example.multitenantsaas.repository.master.TenantRepository;
import com.example.multitenantsaas.security.TenantContext;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger logger = LoggerFactory.getLogger(TenantRoutingDataSource.class);

    private final TenantRepository tenantRepository;
    private final Map<Object, Object> targetDataSources;

    public TenantRoutingDataSource(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
        this.targetDataSources = new HashMap<>();
        setTargetDataSources(targetDataSources);
        setLenientFallback(false); // Fail fast if tenant not found
    }

    public synchronized void addTenantDataSource(Tenant tenant) {
        logger.info("Adding datasource for tenant: {}", tenant.getSubdomain());
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(tenant.getDbUrl());
        ds.setUsername(tenant.getDbUsername());
        ds.setPassword(tenant.getDbPassword());
        targetDataSources.put(tenant.getSubdomain(), ds);
        afterPropertiesSet(); // Refresh resolved datasources
        logger.info("Datasource added for tenant: {}, total datasources: {}", tenant.getSubdomain(), targetDataSources.size());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getCurrentTenant();
        logger.debug("Current tenant lookup key: {}", tenantId);
        return tenantId;
    }

    @Override
    protected synchronized DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        DataSource dataSource = (DataSource) getResolvedDataSources().get(lookupKey);
        if (dataSource == null) {
            logger.error("No datasource found for tenant: {}", lookupKey);
            // Fallback: Try to load from repository
            if (lookupKey != null) {
                Optional<Tenant> tenantOpt = tenantRepository.findBySubdomain((String) lookupKey);
                if (tenantOpt.isPresent()) {
                    Tenant tenant = tenantOpt.get();
                    logger.info("Loading datasource for tenant: {}", tenant.getSubdomain());
                    addTenantDataSource(tenant);
                    dataSource = (DataSource) getResolvedDataSources().get(lookupKey);
                }
            }
        }
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }
}