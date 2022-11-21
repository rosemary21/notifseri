package com.creditville.notifications.sms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BulkSMSDTO {
    private String src;
    private String text;
    private List<String> destinations = new ArrayList<>();

}
