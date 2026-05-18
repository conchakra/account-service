package com.example.accountservice.service.impl;

import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.accountservice.entity.Loan;
import com.example.accountservice.enums.TransactionType;
import com.example.accountservice.repository.LoanRepository;
import com.example.accountservice.service.LoanService;
import com.example.accountservice.dto.CustomerDTO;
import com.example.accountservice.entity.Account;
import com.example.accountservice.repository.AccountRepository;
import org.springframework.http.HttpHeaders;
import com.example.accountservice.entity.AccountBalance;
import com.example.accountservice.entity.AccountTransaction;
import com.example.accountservice.repository.AccountBalanceRepository;
import com.example.accountservice.repository.AccountTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountBalanceRepository accountBalanceRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Override
public List<Loan> getLoansByAccount(String accountNumber) {
    return loanRepository.findByAccountNumber(accountNumber);
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

        String kyc = customer.getKyc();

       if (customer == null || kyc == null ||
   !(kyc.equalsIgnoreCase("KYC Done")
   || kyc.equalsIgnoreCase("YES")
   || kyc.equalsIgnoreCase("ACTIVE"))) {

    throw new RuntimeException("KYC not completed ❌");
}

       loanRequest.setStatus("APPLIED");
loanRequest.setCreatedDate(LocalDateTime.now());
loanRequest.setModifiedDate(LocalDateTime.now());

loanRequest.setDocumentsSubmitted(false);
loanRequest.setDocumentsVerified(false);

loanRequest.setRemainingAmount(loanRequest.getAmount());

loanRequest.setEmiAmount(
        loanRequest.getAmount().divide(
                BigDecimal.valueOf(10),
                2,
                java.math.RoundingMode.HALF_UP
        )
);

return loanRepository.save(loanRequest);
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    // ✅ VERIFY
    @Override
    public Loan verifyLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

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

    // ✅ APPROVE (WITH MONEY CREDIT)
   @Override
public Loan approveLoan(String loanId) {

   Loan loan = loanRepository.findById(loanId)
        .orElseThrow(() -> new RuntimeException("Loan not found"));

if ("APPROVED".equalsIgnoreCase(loan.getStatus())) {
    throw new RuntimeException("Loan already approved");
}

if (!"VERIFICATION_COMPLETE".equalsIgnoreCase(loan.getStatus())
        && !"APPLIED".equalsIgnoreCase(loan.getStatus())) {
    throw new RuntimeException("Loan cannot be approved in current status");
}

    AccountBalance balance = accountBalanceRepository
            .findByAccountNumber(loan.getAccountNumber())
            .orElseThrow(() -> new RuntimeException("Account not found"));

    balance.setAccountBalance(
            balance.getAccountBalance().add(loan.getAmount())
    );

    balance.setModifiedDate(LocalDateTime.now());

    accountBalanceRepository.save(balance);
    syncAccountMainBalance(
    loan.getAccountNumber(),
    balance.getAccountBalance()
);

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

    balance.setAccountBalance(
            balance.getAccountBalance().subtract(emi)
    );
    balance.setModifiedDate(LocalDateTime.now());

    accountBalanceRepository.save(balance);

    syncAccountMainBalance(
    loan.getAccountNumber(),
    balance.getAccountBalance()
);

    AccountTransaction txn = AccountTransaction.builder()
        .accountNumber(loan.getAccountNumber())
        .transactionType(TransactionType.EMI_PAYMENT)
        .amount(emi)
        .transactionDateTime(LocalDateTime.now())
        .build();

    accountTransactionRepository.save(txn);

    loan.setRemainingAmount(
            loan.getRemainingAmount().subtract(emi)
    );

    if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
        loan.setRemainingAmount(BigDecimal.ZERO);
        loan.setStatus("CLOSED");
    } else {
        loan.setStatus("EMI_RUNNING");
    }

    loan.setModifiedDate(LocalDateTime.now());

    return loanRepository.save(loan);
}

    // ✅ DISBURSE
    @Override
    public Loan disburseLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("DISBURSED");
        loan.setModifiedDate(LocalDateTime.now());

        // 💰 CREDIT MONEY HERE (CORRECT PLACE)
        Account account = accountRepository
                .findByAccountNumber(loan.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(
    account.getBalance().add(loan.getAmount())
    );  
        accountRepository.save(account);

        return loanRepository.save(loan);
    }

    // ✅ UPLOAD DOCUMENTS for Representative of document management
    @Override
    public Loan uploadDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setDocumentsSubmitted(true);
        loan.setStatus("DOCUMENTS_UPLOADED");

        return loanRepository.save(loan);
    }

    @Override
    public Loan submitDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("APPLICATION_SUBMITTED");

        return loanRepository.save(loan);
    }

    // ✅ REQUEST DOCUMENTS for Verification
    @Override
    public Loan requestDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("DOCUMENTS_REQUESTED");

        return loanRepository.save(loan);
    }

    @Override
    public Loan verifyDocuments(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setDocumentsVerified(true);
        loan.setStatus("VERIFICATION_COMPLETE");

        return loanRepository.save(loan);
    }

    // ✅ CLOSE
    @Override
    public Loan closeLoan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("CLOSED");
        loan.setModifiedDate(LocalDateTime.now());

        return loanRepository.save(loan);
    }

    // ✅ REJECT
    @Override
public Loan rejectLoan(String loanId) {

    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

    if ("APPROVED".equalsIgnoreCase(loan.getStatus())
        || "EMI_RUNNING".equalsIgnoreCase(loan.getStatus())) {
        throw new RuntimeException("Approved loan cannot be rejected");
    }

    loan.setStatus("REJECTED");
    loan.setModifiedDate(LocalDateTime.now());

    return loanRepository.save(loan);
}
}