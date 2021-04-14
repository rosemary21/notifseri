package com.creditville.notifications.models.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Getter
@Setter
public class HookEvent implements Serializable {
    private String event;
    private HookEventData data;
}
