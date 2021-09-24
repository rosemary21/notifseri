package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class Mobile implements Serializable {
    private String regionCode;
    private String number;
}
