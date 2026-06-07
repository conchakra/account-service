package com.example.accountservice.repository;

import com.example.accountservice.entity.LoanStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanStatementRepository
        extends JpaRepository<LoanStatement, String> {

    List<LoanStatement> findByLoanId(String loanId);
    List<LoanStatement> findByLoanIdAndTransactionDateAfter(String loanId, LocalDateTime fromDate);
}