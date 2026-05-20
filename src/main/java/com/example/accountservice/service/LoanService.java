package com.example.accountservice.service;

import com.example.accountservice.entity.Loan;
import java.util.List;

public interface LoanService {
    Loan applyLoan(Loan loan);

    List<Loan> getAllLoans();

    Loan approveLoan(String loanId);

    Loan verifyLoan(String loanId);

    Loan disburseLoan(String loanId);

    Loan closeLoan(String loanId);

    Loan rejectLoan(String loanId);

    Loan uploadDocuments(String loanId);

    Loan submitDocuments(String loanId);

    Loan requestDocuments(String loanId);

    Loan verifyDocuments(String loanId);

    Loan payEmi(String loanId);

    List<Loan> getLoansByAccount(String accountNumber);
}
