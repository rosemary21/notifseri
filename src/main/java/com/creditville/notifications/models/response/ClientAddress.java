package com.creditville.notifications.models.response;

/* Created by David on 9/1/2021 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ClientAddress implements Serializable {
    @JsonProperty("street1")
    private String street1;
    private String country;
    private String state;
}
