package com.distalgo.saga.payment.repo;

import com.distalgo.saga.payment.entity.UserTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTransactionRepo extends JpaRepository<UserTransaction, Integer> {
}
