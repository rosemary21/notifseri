package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse implements Serializable {
    private String responseCode = "cv00";
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
