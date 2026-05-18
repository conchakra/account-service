package com.example.accountservice.service;

import com.example.accountservice.dto.AccountResponseDto;
import com.example.accountservice.dto.CreateAccountRequestDto;
import com.example.accountservice.entity.AccountTransaction;
import com.example.accountservice.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponseDto createAccount(CreateAccountRequestDto request);

    List<AccountResponseDto> getAllAccounts();

    void credit(String accountNumber, BigDecimal amount);

    void debit(String accountNumber, BigDecimal amount);

    BigDecimal getBalance(String accountNumber);

    List<AccountTransaction> getTransactions(String accountNumber);

    Account applyInterest(String accountNumber);

}