package com.example.accountservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequestDto {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Balance is required")
    @Min(value = 0, message = "Balance cannot be negative")
    private BigDecimal balance;

    @NotBlank(message = "Modified By is required")
    private String modifiedBy;
}