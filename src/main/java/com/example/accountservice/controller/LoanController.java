package com.example.accountservice.controller;

import com.example.accountservice.entity.Loan;
import com.example.accountservice.entity.LoanStatement;
import com.example.accountservice.service.LoanService;
import org.springframework.web.bind.annotation.*;
import com.example.accountservice.repository.LoanStatementRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "http://localhost:3000")
public class LoanController {

    private final LoanService loanService;
    private final LoanStatementRepository loanStatementRepository;

    public LoanController(LoanService loanService, LoanStatementRepository loanStatementRepository) {
        this.loanService = loanService;
        this.loanStatementRepository = loanStatementRepository;
    }

    @PostMapping("/apply")
    public Loan applyLoan(@RequestBody Loan loan) {
        return loanService.applyLoan(loan);
    }

   @GetMapping("/statement/{loanId}")
public List<LoanStatement> getLoanStatement(
        @PathVariable String loanId) {

    LocalDateTime fromDate =
            LocalDateTime.now().minusMonths(3);

    return loanStatementRepository
            .findByLoanIdAndTransactionDateAfter(
                    loanId,
                    fromDate);
}

  @GetMapping("/customer/{customerId}")
public List<Loan> getLoansByCustomer(
        @PathVariable String customerId) {

    System.out.println("LOAN CUSTOMER API HIT");

    return loanService.getLoansByCustomer(customerId);
}

    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @PutMapping("/{id}/approve")
    public Loan approveLoan(@PathVariable String id) {
        return loanService.approveLoan(id);
    }

    @PutMapping("/{id}/reject")
    public Loan rejectLoan(@PathVariable String id) {
        return loanService.rejectLoan(id);
    }

    @PutMapping("/{id}/close")
    public Loan closeLoan(@PathVariable String id) {
        return loanService.closeLoan(id);
    }

    @PostMapping("/{loanId}/pay-emi")
    public Loan payEmi(@PathVariable String loanId) {
        return loanService.payEmi(loanId);
    }

    @GetMapping("/account/{accountNumber}")
    public List<Loan> getLoansByAccount(@PathVariable String accountNumber) {
        return loanService.getLoansByAccount(accountNumber);
    }
}
