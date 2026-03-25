package com.example.accountservice.dto;

import com.example.accountservice.enums.AccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AccountResponseDto {
    private String id;
    private String accountNumber;
    private String customerId;
    private LocalDateTime openingDate;
    private AccountStatus status;
    private LocalDateTime modifiedDate;
}