package com.creditville.notifications.disburse.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class Payments {
    private BigDecimal amount;
    private String paymentMethodName;
    private String paymentType;
    private String accountID;
    private String shareClassName;
    private String fee;
    private String interbankTransfer;
}
