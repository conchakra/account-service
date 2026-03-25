package com.example.accountservice.service.impl;

import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import com.example.accountservice.entity.Loan;
import com.example.accountservice.repository.LoanRepository;
import com.example.accountservice.service.LoanService;
import com.example.accountservice.dto.CustomerDTO;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

  @Override
public Loan applyLoan(Loan loanRequest) {

    // 🔥 CALL CUSTOMER SERVICE
    RestTemplate restTemplate = new RestTemplate();

    String url = "http://localhost:8081/customers/" + loanRequest.getCustomerId();

    CustomerDTO customer = restTemplate.getForObject(url, CustomerDTO.class);

    if (customer == null || !customer.isKycVerified()) {
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