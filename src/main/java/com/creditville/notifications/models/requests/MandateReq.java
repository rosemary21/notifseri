package com.creditville.notifications.models.requests;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MandateReq {

    private String merchantId;
    private String mandateId;
    private String hash;
    private String requestId;
    private String transactionRef;
}
