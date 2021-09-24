package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResp {

    private String code;
    private String message;
    private String fieldId;
}
