package com.example.accountservice.service;

import com.example.accountservice.entity.Loan;
import java.util.List;

public interface LoanService {

    Loan applyLoan(Loan loan);

    List<Loan> getAllLoans();

    Loan approveLoan(String loanId);

    Loan rejectLoan(String loanId);
}