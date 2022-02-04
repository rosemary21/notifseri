package com.creditville.notifications.models.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemitaDebitStatus {
    private String merchantId;
    private String mandateId;
    private String hash;
    private String requestId;
}
