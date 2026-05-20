package com.example.accountservice.service.impl;

import com.example.accountservice.dto.CustomerDTO;
import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.AccountBalance;
import com.example.accountservice.entity.AccountTransaction;
import com.example.accountservice.entity.Loan;
import com.example.accountservice.repository.AccountBalanceRepository;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.AccountTransactionRepository;
import com.example.accountservice.repository.LoanRepository;
import com.example.accountservice.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Loan applyLoan(Loan loanRequest) {
        String url = "http://localhost:8081/customer/api/v1/customers/" + loanRequest.getCustomerId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer YOUR_TOKEN_HERE");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<CustomerDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CustomerDTO.class);

        CustomerDTO customer = response.getBody();
        if (customer == null || customer.getKyc() == null || !"KYC Done".equalsIgnoreCase(customer.getKyc().trim())) {
            throw new RuntimeException("KYC not completed ❌");
        }

        loanRequest.setStatus("APPLIED");
        loanRequest.setCreatedDate(LocalDateTime.now());
        loanRequest.setModifiedDate(LocalDateTime.now());
        loanRequest.setDocumentsSubmitted(false);
        loanRequest.setDocumentsVerified(false);
        loanRequest.setRemainingAmount(loanRequest.getAmount());
        loanRequest.setEmiAmount(loanRequest.getAmount().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP));

        return loanRepository.save(loanRequest);
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public List<Loan> getLoansByAccount(String accountNumber) {
        return loanRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Loan verifyLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("VERIFICATION_COMPLETE");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan approveLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if ("APPROVED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Loan already approved");
        }

        if (!"VERIFICATION_COMPLETE".equalsIgnoreCase(loan.getStatus()) && !"APPLIED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Loan cannot be approved in current status");
        }

        AccountBalance balance = accountBalanceRepository
                .findByAccountNumber(loan.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        balance.setAccountBalance(balance.getAccountBalance().add(loan.getAmount()));
        balance.setModifiedDate(LocalDateTime.now());
        accountBalanceRepository.save(balance);

        syncAccountMainBalance(loan.getAccountNumber(), balance.getAccountBalance());

        loan.setStatus("APPROVED");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan rejectLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if ("APPROVED".equalsIgnoreCase(loan.getStatus()) || "EMI_RUNNING".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Approved loan cannot be rejected");
        }

        loan.setStatus("REJECTED");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan closeLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("CLOSED");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan payEmi(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if ("REJECTED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Rejected loan cannot pay EMI");
        }

        if ("APPLIED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Approve loan first before EMI");
        }

        if ("CLOSED".equalsIgnoreCase(loan.getStatus())) {
            throw new RuntimeException("Loan already closed");
        }

        AccountBalance balance = accountBalanceRepository
                .findByAccountNumber(loan.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        BigDecimal emi = loan.getEmiAmount();
        if (balance.getAccountBalance().compareTo(emi) < 0) {
            throw new RuntimeException("Insufficient balance for EMI");
        }

        balance.setAccountBalance(balance.getAccountBalance().subtract(emi));
        balance.setModifiedDate(LocalDateTime.now());
        accountBalanceRepository.save(balance);

        syncAccountMainBalance(loan.getAccountNumber(), balance.getAccountBalance());

        AccountTransaction txn = AccountTransaction.builder()
                .accountNumber(loan.getAccountNumber())
                .transactionType(com.example.accountservice.enums.TransactionType.EMI_PAYMENT)
                .amount(emi)
                .transactionDateTime(LocalDateTime.now())
                .build();
        accountTransactionRepository.save(txn);

        loan.setRemainingAmount(loan.getRemainingAmount().subtract(emi));
        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setRemainingAmount(BigDecimal.ZERO);
            loan.setStatus("CLOSED");
        } else {
            loan.setStatus("EMI_RUNNING");
        }
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan disburseLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("DISBURSED");
        loan.setModifiedDate(LocalDateTime.now());

        Account account = accountRepository
                .findByAccountNumber(loan.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(account.getBalance().add(loan.getAmount()));
        accountRepository.save(account);

        return loanRepository.save(loan);
    }

    @Override
    public Loan uploadDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setDocumentsSubmitted(true);
        loan.setStatus("DOCUMENTS_UPLOADED");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan submitDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("APPLICATION_SUBMITTED");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan requestDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("DOCUMENTS_REQUESTED");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Override
    public Loan verifyDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setDocumentsVerified(true);
        loan.setStatus("VERIFICATION_COMPLETE");
        loan.setModifiedDate(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    private void syncAccountMainBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(newBalance);
        account.setModifiedDate(LocalDateTime.now());
        accountRepository.save(account);
    }
}
