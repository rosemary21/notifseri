package com.creditville.notifications.disburse.dto;

import lombok.Data;

@Data
public class DisbursePaymentResponse {
    private String amount;
    private String paymentMethodName;
}
