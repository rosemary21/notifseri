package com.creditville.notifications.models.requests;

/* Created by David on 9/23/2021 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
public class RemitaHookEvent {
    private String notificationType;
    private List<LineItem> lineItems;

    @ToString
    @Getter
    @Setter
    public class LineItem {
        private String mandateId;
        private String activationDate;
        private String debitDate;//Debit Only...
        private String requestId;
        private String startDate;
        private String endDate;
        private String amount;
    }
}
