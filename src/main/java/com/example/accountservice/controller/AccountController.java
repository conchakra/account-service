package com.example.accountservice.controller;

import com.example.accountservice.dto.AccountResponseDto;
import com.example.accountservice.dto.CreateAccountRequestDto;
import com.example.accountservice.entity.Account;
import com.example.accountservice.service.AccountService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto createAccount(@RequestBody CreateAccountRequestDto request) {
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
    public ResponseEntity<java.math.BigDecimal> getBalance(@PathVariable String accountNumber) {
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
