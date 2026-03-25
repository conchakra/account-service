package com.example.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.accountservice.entity.Loan;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, String> {

    List<Loan> findByCustomerId(String customerId);

}