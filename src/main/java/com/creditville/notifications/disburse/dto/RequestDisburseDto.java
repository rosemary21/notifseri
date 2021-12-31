package com.creditville.notifications.disburse.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Lob;
import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class RequestDisburseDto {
    private String clientId;
    private String clientName;
    private String accountName;
    private String appKeyId;
    private String loanId;
    private BigDecimal amount;
    private String accountState;
    private String repaymentMethod;
    private String repaymentBank;
    private String repaymentAccountNo;
    private String loanAccount;
    private String firstRepaymentMethod;
    private String bankCode;
    private boolean topUpStatus;
    private BigDecimal activeAmount;
    private String activeLoanId;
    private String amountWithInterest;
    private String modeOfRepayment;
    private String remitaState;
    private String status;
    private Long id;
    private String emailAddress;
    @Lob
    private String emailMessage;
    private String emailTo;

}
