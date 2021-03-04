package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Chuks on 02/09/2021.
 */
@Getter
@Setter
public class LookupLoan implements Serializable {
    @JsonProperty("ID")
    private String id;
    private LookUpLoanAccount account;
}
