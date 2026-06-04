package com.example.accountservice.controller;

import com.example.accountservice.dto.AccountResponseDto;
import com.example.accountservice.dto.CreateAccountRequestDto;
import com.example.accountservice.entity.Account;
import com.example.accountservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

   @GetMapping("/customer/{customerId}")
public ResponseEntity<AccountResponseDto> getAccountByCustomer(
        @PathVariable String customerId) {

    System.out.println("ACCOUNT CUSTOMER API HIT");

    return ResponseEntity.ok(
            accountService.getAccountByCustomer(customerId));
}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto createAccount(
            @Valid @RequestBody CreateAccountRequestDto request) {
        return accountService.createAccount(request);
    }

    @GetMapping
    public List<AccountResponseDto> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<String> credit(@PathVariable String accountNumber,
                                         @RequestBody Map<String, BigDecimal> request) {
        accountService.credit(accountNumber, request.get("amount"));
        return ResponseEntity.ok("Amount credited successfully");
    }

    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<String> debit(@PathVariable String accountNumber,
                                        @RequestBody Map<String, BigDecimal> request) {
        accountService.debit(accountNumber, request.get("amount"));
        return ResponseEntity.ok("Amount debited successfully");
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber));
    }

    @GetMapping("/{accountNumber}/transactions")
    public Object getTransactions(@PathVariable String accountNumber) {
        return accountService.getTransactions(accountNumber);
    }

    @PostMapping("/{accountNumber}/interest")
    public ResponseEntity<Account> applyInterest(@PathVariable String accountNumber) {
        Account updated = accountService.applyInterest(accountNumber);
        return ResponseEntity.ok(updated);
    }
}
