package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Chuks on 02/09/2021.
 */
@Getter
@Setter
public class LookUpLoanAccount implements Serializable {
    private LookupLoanLoanAccount loanAccount;
    private Client client;
    private LookUpLoanSuggestedSchedule suggestedSchedule;
}
