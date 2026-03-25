package com.example.accountservice.entity;

import com.example.accountservice.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;

    @Column(name = "opening_date", nullable = false)
    private LocalDateTime openingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(nullable = false)
    private Double balance = 0.0;

    @Column(nullable = false)
    private Double interestRate = 5.0;
}