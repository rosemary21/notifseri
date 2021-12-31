package com.creditville.notifications.disburse.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class LookupDisburse {
    private String accountID;
    private String disbursementDate;
    private String firstRepaymentDate;
    private List<Payments> payments;

}
