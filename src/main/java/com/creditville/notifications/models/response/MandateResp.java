package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MandateResp {
    private String statuscode;
    @JsonProperty("isActive")
    private boolean isActive;
    private String mandateId;
    private String status;
    private String transactionRef;
    private String rrr;

    private ValidationResp validation;
}
