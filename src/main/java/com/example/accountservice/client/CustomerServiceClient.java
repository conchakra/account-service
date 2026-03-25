package com.example.accountservice.client;

import com.example.accountservice.dto.CustomerVerificationResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerServiceClient {

    private final RestTemplate restTemplate;

    @Value("${customer.service.base-url}")
    private String customerServiceBaseUrl;

    public CustomerServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyCustomer(String customerId) {
        String url = customerServiceBaseUrl + "/customer/api/v1/customers/verify/" + customerId;

        CustomerVerificationResponseDto response = restTemplate.getForObject(url,
                CustomerVerificationResponseDto.class);

        return response != null && response.isSuccess();
    }
}