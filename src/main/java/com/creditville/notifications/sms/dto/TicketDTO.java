package com.creditville.notifications.sms.dto;

import java.util.ArrayList;
import java.util.List;

public class TicketDTO {
    List<String> ticketIds=new ArrayList<>();

    public List<String> getTicketIds() {
        return ticketIds;
    }

    public void setTicketIds(List<String> ticketIds) {
        this.ticketIds = ticketIds;
    }
}
