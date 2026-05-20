package com.example.accountservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String customerId;

    private String accountNumber;

    private BigDecimal amount;

    private BigDecimal remainingAmount;

    private BigDecimal emiAmount;

    private String status;

    private String role;

    private String remarks;

    private Boolean documentsSubmitted = false;

    private Boolean documentsVerified = false;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;
}
