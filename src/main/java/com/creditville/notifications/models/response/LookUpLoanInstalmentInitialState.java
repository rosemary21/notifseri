package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Created by Chuks on 02/08/2021.
 */
@Getter
@Setter
public class LookUpLoanInstalmentInitialState {
    private BigDecimal principalDueAmount;
    private BigDecimal loanAccountBalance;
}
