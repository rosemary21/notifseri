package com.creditville.notifications.instafin.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepayLoanReq {

    private String accountID;
    private String paymentMethodName;
    private String repaymentDate;
    private BigDecimal amount;
    private String chequeNumber;
    private String chequeBankID;
    private String referenceNumber;
    private String transactionBranchID;
    private String notes;
    private String transactionID;
}
