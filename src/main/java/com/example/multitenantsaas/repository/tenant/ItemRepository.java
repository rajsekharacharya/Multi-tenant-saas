package com.example.multitenantsaas.repository.tenant;

import com.example.multitenantsaas.domain.tenant.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}