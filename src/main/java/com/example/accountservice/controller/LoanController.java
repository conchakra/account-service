package com.example.accountservice.controller;

import com.example.accountservice.entity.Loan;
import com.example.accountservice.service.LoanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000") // Allow requests from React frontend
@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // APPLY LOAN
    @PostMapping("/apply")
    public Loan applyLoan(@RequestBody Loan loan) {
        return loanService.applyLoan(loan);
    }

    // GET ALL LOANS
    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    // APPROVE LOAN
    @PutMapping("/{id}/approve")
    public Loan approveLoan(@PathVariable String id) {
        return loanService.approveLoan(id);
    }

    @PostMapping("/{loanId}/pay-emi")
public Loan payEmi(@PathVariable String loanId) {
    return loanService.payEmi(loanId);
}

@GetMapping("/account/{accountNumber}")
public List<Loan> getLoansByAccount(@PathVariable String accountNumber) {
    return loanService.getLoansByAccount(accountNumber);
}

    // REJECT LOAN
    @PutMapping("/{id}/reject")
    public Loan rejectLoan(@PathVariable String id) {
        return loanService.rejectLoan(id);
    }

    @PutMapping("/{id}/close")
public Loan closeLoan(@PathVariable String id) {
    return loanService.closeLoan(id);
}
}