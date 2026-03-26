package com.example.accountservice.service.impl;

import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.accountservice.entity.Loan;
import com.example.accountservice.repository.LoanRepository;
import com.example.accountservice.service.LoanService;
import com.example.accountservice.dto.CustomerDTO;
import com.example.accountservice.entity.Account;
import com.example.accountservice.repository.AccountRepository;
import org.springframework.http.HttpHeaders;
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

    @Override
    public Loan applyLoan(Loan loanRequest) {

        String url = "http://localhost:8081/customer/api/v1/customers/" + loanRequest.getCustomerId();

        // 🔥 ADD HEADERS
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

        if (customer == null || kyc == null || !kyc.trim().equalsIgnoreCase("KYC Done")) {
            throw new RuntimeException("KYC not completed ❌");
        }

        loanRequest.setStatus("PENDING");
        loanRequest.setCreatedDate(LocalDateTime.now());

        return loanRepository.save(loanRequest);
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Loan approveLoan(String loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("APPROVED");
        loan.setModifiedDate(LocalDateTime.now());

        // 🔥 CREDIT MONEY TO ACCOUNT
        Account account = accountRepository
                .findByAccountNumber(loan.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance() + loan.getAmount());
        accountRepository.save(account);

        return loanRepository.save(loan);
    }

    @Override
    public Loan rejectLoan(String loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus("REJECTED");
        loan.setModifiedDate(LocalDateTime.now());

        return loanRepository.save(loan);
    }
}