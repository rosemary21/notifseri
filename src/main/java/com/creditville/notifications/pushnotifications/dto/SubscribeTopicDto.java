package com.creditville.notifications.pushnotifications.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscribeTopicDto {
    private String fcmToken;
    private String topic;

}
