package com.creditville.notifications.sms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDTO {
    private String errorCode;
    private String errorMessage;
    private String destination;
    private String ticketId;
    private String status;

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", destination='" + destination + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}
