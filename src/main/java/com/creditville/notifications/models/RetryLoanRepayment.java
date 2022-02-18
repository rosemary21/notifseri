package com.creditville.notifications.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
public class RetryLoanRepayment {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private BigDecimal amount;
    private String loanId;
    private String clientId;
    private String transactionDate;
    private int noOfRetry;
    private String processFlag="N";
    private String status;
    private String reference;
    private String manualStatus="N";
    private String email;
    private String instafinObliDate;

}
