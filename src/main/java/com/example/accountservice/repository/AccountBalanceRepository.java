package com.example.accountservice.repository;

import com.example.accountservice.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, String> {
    Optional<AccountBalance> findByAccountNumber(String accountNumber);
}