package com.example.accountservice.service.impl;

import com.example.accountservice.client.CustomerServiceClient;
import com.example.accountservice.dto.AccountResponseDto;
import com.example.accountservice.dto.CreateAccountRequestDto;
import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.AccountAudit;
import com.example.accountservice.entity.AccountBalance;
import com.example.accountservice.entity.AccountSeries;
import com.example.accountservice.entity.AccountTransaction;
import com.example.accountservice.enums.AccountStatus;
import com.example.accountservice.enums.TransactionType;
import com.example.accountservice.repository.AccountAuditRepository;
import com.example.accountservice.repository.AccountBalanceRepository;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.AccountSeriesRepository;
import com.example.accountservice.repository.AccountTransactionRepository;
import com.example.accountservice.service.AccountService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountAuditRepository accountAuditRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final AccountSeriesRepository accountSeriesRepository;
    private final CustomerServiceClient customerServiceClient;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            AccountTransactionRepository accountTransactionRepository,
            AccountAuditRepository accountAuditRepository,
            AccountBalanceRepository accountBalanceRepository,
            AccountSeriesRepository accountSeriesRepository,
            CustomerServiceClient customerServiceClient) {

        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
        this.accountAuditRepository = accountAuditRepository;
        this.accountBalanceRepository = accountBalanceRepository;
        this.accountSeriesRepository = accountSeriesRepository;
        this.customerServiceClient = customerServiceClient;
    }

    @Override
    public List<AccountResponseDto> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public AccountResponseDto createAccount(CreateAccountRequestDto request) {

        boolean customerExists = customerServiceClient.verifyCustomer(request.getCustomerId());
        if (!customerExists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Customer not found with id: " + request.getCustomerId());
        }

        if (accountRepository.existsByCustomerId(request.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Duplicate account creation attempt for customer id: " + request.getCustomerId());
        }

        String accountNumber = generateAccountNumber();
        LocalDateTime now = LocalDateTime.now();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .customerId(request.getCustomerId())
                .openingDate(now)
                .status(AccountStatus.ACTIVE)
                .modifiedDate(now)
                .build();

        Account savedAccount = accountRepository.save(account);

        AccountBalance balance = AccountBalance.builder()
                .accountNumber(accountNumber)
                .accountBalance(BigDecimal.ZERO)
                .modifiedDate(now)
                .build();

        accountBalanceRepository.save(balance);

        saveAudit(savedAccount, "CREATE", request.getModifiedBy());

        return mapToResponse(savedAccount);
    }

    private String generateAccountNumber() {
        AccountSeries activeSeries = accountSeriesRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No active account series found"));

        int nextSequence = activeSeries.getLastSequence() + 1;

        if (nextSequence > 99999) {
            activeSeries.setIsActive(false);
            activeSeries.setModifiedDate(LocalDateTime.now());
            accountSeriesRepository.save(activeSeries);

            int nextSeriesValue = Integer.parseInt(activeSeries.getSeries()) + 1;

            AccountSeries newSeries = AccountSeries.builder()
                    .series(String.valueOf(nextSeriesValue))
                    .lastSequence(1)
                    .isActive(true)
                    .modifiedDate(LocalDateTime.now())
                    .build();

            accountSeriesRepository.save(newSeries);

            return formatAccountNumber(newSeries.getSeries(), 1);
        }

        activeSeries.setLastSequence(nextSequence);
        activeSeries.setModifiedDate(LocalDateTime.now());
        accountSeriesRepository.save(activeSeries);

        return formatAccountNumber(activeSeries.getSeries(), nextSequence);
    }

    private String formatAccountNumber(String series, int sequence) {
        return series + "-2603-" + String.format("%05d", sequence);
    }

    private void saveAudit(Account account, String action, String modifiedBy) {
        AccountAudit audit = AccountAudit.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .openingDate(account.getOpeningDate())
                .status(account.getStatus())
                .action(action)
                .modifiedBy(modifiedBy)
                .modifiedDate(LocalDateTime.now())
                .build();

        accountAuditRepository.save(audit);
    }

    private AccountResponseDto mapToResponse(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .openingDate(account.getOpeningDate())
                .status(account.getStatus())
                .modifiedDate(account.getModifiedDate())
                .build();
    }

    @Override
    @Transactional
    public void credit(String accountNumber, BigDecimal amount) {

        AccountBalance balance = accountBalanceRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        balance.setAccountBalance(balance.getAccountBalance().add(amount));
        balance.setModifiedDate(LocalDateTime.now());

        accountBalanceRepository.save(balance);

        AccountTransaction txn = AccountTransaction.builder()
                .accountNumber(accountNumber)
                .transactionType(TransactionType.CREDIT)
                .amount(amount)
                .transactionDateTime(LocalDateTime.now())
                .build();

        accountTransactionRepository.save(txn);
    }

    @Override
    @Transactional
    public void debit(String accountNumber, BigDecimal amount) {

        AccountBalance balance = accountBalanceRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        balance.setAccountBalance(balance.getAccountBalance().subtract(amount));
        balance.setModifiedDate(LocalDateTime.now());

        accountBalanceRepository.save(balance);

        AccountTransaction txn = AccountTransaction.builder()
                .accountNumber(accountNumber)
                .transactionType(TransactionType.DEBIT)
                .amount(amount)
                .transactionDateTime(LocalDateTime.now())
                .build();

        accountTransactionRepository.save(txn);
    }

    @Override
    public BigDecimal getBalance(String accountNumber) {
        AccountBalance balance = accountBalanceRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return balance.getAccountBalance();
    }

    @Override
    public List<AccountTransaction> getTransactions(String accountNumber) {
        return accountTransactionRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Account applyInterest(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        double rate = account.getInterestRate();
        double interest = (account.getBalance() * rate) / 100;

        // Update balance
        account.setBalance(account.getBalance() + interest);
        accountRepository.save(account);

        // Save transaction
        AccountTransaction txn = new AccountTransaction();
        txn.setAccountNumber(account.getAccountNumber());
        txn.setTransactionType(TransactionType.INTEREST);
        txn.setAmount(BigDecimal.valueOf(interest));
        txn.setTransactionDateTime(LocalDateTime.now());

        accountTransactionRepository.save(txn);

        return account;
    }
}