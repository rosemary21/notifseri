package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Created by Chuks on 02/08/2021.
 */
@Getter
@Setter
public class LookUpLoanInstalmentCurrentState {
    private BigDecimal principalDueAmount;
    private BigDecimal interestDueAmount;
    @JsonProperty(value = "feeDueAmount")
    private BigDecimal feeDueAmount;
}
