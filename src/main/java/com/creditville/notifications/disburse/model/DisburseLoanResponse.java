package com.creditville.notifications.disburse.model;

import com.creditville.notifications.disburse.dto.DisbursePaymentResponse;
import lombok.Data;

import java.util.List;

@Data
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
