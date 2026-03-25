package com.example.accountservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequestDto {
    private String customerId;
    private String modifiedBy;
}