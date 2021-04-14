package com.creditville.notifications.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@ToString
@Getter
@Setter
public class HookEventData implements Serializable {
    private String id;
    private String domain;
    private String status;
    private String reference;
    private BigDecimal amount;
    private String message;
    @JsonProperty("gateway_response")
    private String gatewayResponse;
    @JsonProperty("paid_at")
    private String paidAt;
    @JsonProperty("created_at")
    private String createdAt;
    private String channel;
    private String currency;
    private HookEventCustomer customer;
    private HookEventAuthorization authorization;
}
