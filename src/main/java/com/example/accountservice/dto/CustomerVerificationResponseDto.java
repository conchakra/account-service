package com.example.accountservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CustomerVerificationResponseDto {
    private boolean success;
    private String customerId;
    private String message;
}