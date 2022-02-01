package com.creditville.notifications.disburse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DisburseLoanResponse {
    private String accountID;
    private String fundingSourceID;
    private String disbursementDate;
    private String firstRepaymentDate;
    private String secondInstalmentDate;
    private String chequeNumber;
    private String chequeBankID;
    private String referenceNumber;
    private String transactionBranchID;
    private String notes;
    private String transactionID;
    private String bankCode;
    private List<DisbursePaymentResponse> payments;
}
