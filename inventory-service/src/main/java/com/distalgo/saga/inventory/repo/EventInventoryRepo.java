package com.distalgo.saga.inventory.repo;

import com.distalgo.saga.inventory.entity.EventInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventInventoryRepo extends JpaRepository<EventInventory, Integer> {

}
