package com.creditville.notifications.sms.dto.vtpass;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VtpassRequestDto {
    private String message;
    private String recipient;
    private String responsetype;
    private String sender;
}
