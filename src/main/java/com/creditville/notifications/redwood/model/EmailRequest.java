package com.creditville.notifications.redwood.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {
    private String name;
    private String email;
    private String address;
    private String message;


}
