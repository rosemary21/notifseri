package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class SuccessResponse implements Serializable {
    private Integer responseCode = 99;
    private String message;
    private Object responseData;

    public SuccessResponse(String message) {
        this.message = message;
    }

    public SuccessResponse(String message, Object responseData) {
        this.message = message;
        this.responseData = responseData;
    }
}
