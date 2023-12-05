package com.creditville.notifications.models.DTOs;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RetryLoanRepaymentDTO {

    private Long id;
    private BigDecimal amount;
    private String loanId;
    private String clientId;
    private String transactionDate;
    private int noOfRetry;
    private String processFlag;
    private String status;
    private String reference;
    private String email;
    private String instafinObliDate;
    private String methodOfRepayment;
    private String mandateId;
    private BigDecimal instafinRepayAmt;


}
