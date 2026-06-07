package com.example.accountservice.repository;

import com.example.accountservice.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, String> {
    List<AccountTransaction> findByAccountNumber(String accountNumber);
    List<AccountTransaction> findByAccountNumberAndTransactionDateTimeAfter(String accountNumber,
         LocalDateTime fromDate);

         
}