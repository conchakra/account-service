package com.example.accountservice.entity;

import com.example.accountservice.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "action")
    private String action;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}