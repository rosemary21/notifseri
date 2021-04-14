package com.creditville.notifications.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Getter
@Setter
public class HookEventAuthorization implements Serializable {
    @JsonProperty("authorization_code")
    private String authorizationCode;
    @JsonProperty("exp_month")
    private String expMonth;
    @JsonProperty("exp_year")
    private String expYear;
    @JsonProperty("card_type")
    private String cardType;
    private String bank;
    @JsonProperty("country_code")
    private String countryCode;
    private String brand;
    @JsonProperty("account_name")
    private String accountName;
}
