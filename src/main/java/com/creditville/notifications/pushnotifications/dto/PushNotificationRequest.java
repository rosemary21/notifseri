package com.creditville.notifications.pushnotifications.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushNotificationRequest {
    private String title;
    private String message;
}
