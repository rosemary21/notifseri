package com.creditville.notifications.models.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargeDto {

    private String authorization_code;
    private String email;
    private BigDecimal amount;
}
