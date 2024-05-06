package com.distalgo.saga.payment.repo;

import com.distalgo.saga.payment.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceRepo extends JpaRepository<UserBalance, Integer> {
}