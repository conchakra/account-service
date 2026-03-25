package com.example.accountservice.entity;

import jakarta.persistence.*;
import lombok.*;
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

    private Double amount;

    private String status; // PENDING, APPROVED, REJECTED

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;
}