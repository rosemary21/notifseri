package com.creditville.notifications.disburse.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListenRecipientData {
    BigDecimal amount;
    private String createdAt;
    private String currency;
    private String domain;
    private String failures;
    private Long id;
    private Long integration;
    private String reason;
    private String reference;
    private String source;
    @JsonProperty("source_details")
    private String sourceDetails;
    private String status;
    @JsonProperty("titan_code")
    private String titanCode;
    @JsonProperty("transfer_code")
    private String transferCode;
    @JsonProperty("transferred_at")
    private String transferredAt;
    private String updateAt;



}
