package com.example.accountservice.controller;

import org.springframework.web.bind.annotation.*;
import com.example.accountservice.entity.Loan;
import com.example.accountservice.service.LoanService;
import java.util.List;

@RestController
@RequestMapping("/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // APPLY LOAN
    @PostMapping
    public Loan applyLoan(@RequestBody Loan loan) {
        return loanService.applyLoan(loan);
    }

    // GET ALL
    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    // APPROVE
    @PostMapping("/{loanId}/approve")
    public Loan approveLoan(@PathVariable String loanId) {
        return loanService.approveLoan(loanId);
    }

    // REJECT
    @PostMapping("/{loanId}/reject")
    public Loan rejectLoan(@PathVariable String loanId) {
        return loanService.rejectLoan(loanId);
    }
}