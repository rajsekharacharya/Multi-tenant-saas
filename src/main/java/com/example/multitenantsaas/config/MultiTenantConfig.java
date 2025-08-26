package com.example.multitenantsaas.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.multitenantsaas.repository.master.TenantRepository;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.multitenantsaas.repository.tenant",
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager")
public class MultiTenantConfig {

    private final TenantRepository tenantRepository;
    private final JpaProperties jpaProperties;

    public MultiTenantConfig(TenantRepository tenantRepository, JpaProperties jpaProperties) {
        this.tenantRepository = tenantRepository;
        this.jpaProperties = jpaProperties;
    }

    @Bean
    public DataSource tenantDataSource() {
        return new TenantRoutingDataSource(tenantRepository);
    }

    @Bean
    public TenantRoutingDataSource tenantRoutingDataSource() {
        return new TenantRoutingDataSource(tenantRepository);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(tenantDataSource());
        em.setPackagesToScan("com.example.multitenantsaas.domain.tenant");

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        properties.put("hibernate.hbm2ddl.auto", "none");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager tenantTransactionManager() {
        return new JpaTransactionManager(tenantEntityManagerFactory().getObject());
    }
}