package com.example.accountservice.repository;

import com.example.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByCustomerId(String customerId);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByCustomerId(String customerId);
}