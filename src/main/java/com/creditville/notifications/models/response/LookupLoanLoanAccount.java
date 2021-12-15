package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Chuks on 02/09/2021.
 */
@Getter
@Setter
public class LookupLoanLoanAccount {
    private String accountStatus;
    private Date createdOn;
    private List<LookUpLoanInstalment> instalments;
    private LookUpLoanLoanAccountOptionalFields optionalFields;
    private BigDecimal loanAmount;
   private  String maximumRepayableAsOfToday;
}
