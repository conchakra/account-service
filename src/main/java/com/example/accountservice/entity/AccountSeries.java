package com.example.accountservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String series; // example 1400

    @Column(name = "last_sequence", nullable = false)
    private Integer lastSequence;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}