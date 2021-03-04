package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Chuks on 02/08/2021.
 */
@Getter
@Setter
public class LookUpClientLoan implements Serializable {
    @JsonProperty("ID")
    private String id;
    private String status;
    private String amount;
    private String closeDate;
}
