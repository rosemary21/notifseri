package com.creditville.notifications.models.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartialDebitDto {

    private String authorization_code;
    private String currency;
    private BigDecimal amount;
    private String email;
    private String at_least;

    public PartialDebitDto() {
    }

    public PartialDebitDto(String authorization_code, String currency, BigDecimal amount, String email, String at_least) {
        this.authorization_code = authorization_code;
        this.currency = currency;
        this.amount = amount;
        this.email = email;
        this.at_least = at_least;
    }

    public PartialDebitDto(String authorization_code, BigDecimal amount, String email) {
        this.authorization_code = authorization_code;
        this.currency = "NGN";
        this.amount = amount;
        this.email = email;
        this.at_least = "2000";
    }
}
