package com.distalgo.saga.inventory.repo;

import com.distalgo.saga.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepo extends JpaRepository<InventoryTransaction, Integer> {
}

