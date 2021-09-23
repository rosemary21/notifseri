package com.creditville.notifications.models.DTOs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class MandateDTO {
    private String clientId;
    private String loanId;
    private BigDecimal amount;
    private String accountNumber;
    private String bankCode;
    private String startDate;
    private String endDate;
}
