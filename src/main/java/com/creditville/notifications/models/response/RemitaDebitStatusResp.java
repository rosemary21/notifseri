package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemitaDebitStatusResp {
    private String statuscode;
    private BigDecimal amount;
    private String RRR;
    private String requestId;
    private String mandateId;
    private String transactionRef;
    private String lastStatusUpdateTime;
    private String status;
}
