package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LookUpLoanSuggestedSchedule {
    private String scheduleStartDate;
    private String secondInstalmentDate;
    private String scheduleEndDate;
}
