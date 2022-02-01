package com.creditville.notifications.sms.dto;

import java.util.ArrayList;
import java.util.List;

public class MultipleSMSDTO {

    List<RequestDTO> requests=new ArrayList<>();

    public List<RequestDTO> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestDTO> requests) {
        this.requests = requests;
    }

    @Override
    public String toString() {
        return "MultipleSMSDTO{" +
                "requests=" + requests +
                '}';
    }
}
