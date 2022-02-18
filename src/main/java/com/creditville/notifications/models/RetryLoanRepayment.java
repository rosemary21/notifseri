package com.creditville.notifications.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@Setter
@Getter
public class RetryLoanRepayment {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;
    private BigDecimal amount;
    private String loanId;
    private String clientId;
    private String transactionDate;
    private int noOfRetry;
    private String processFlag;
    private String status;
}
