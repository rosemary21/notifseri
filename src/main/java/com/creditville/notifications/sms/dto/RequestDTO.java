package com.creditville.notifications.sms.dto;

import lombok.Data;

import javax.persistence.Lob;

@Data
public class RequestDTO {
    private String dest;
    private String src;
    @Lob
    private String text;
    private String ticketId;
    private boolean unicode;
}
