package com.creditville.notifications.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Chuks on 02/08/2021.
 */
@Getter
@Setter
public class LookUpLoanInstalment implements Serializable {
    @JsonProperty("ID")
    private String id;
    private String obligatoryPaymentDate;
    private LookUpLoanInstalmentInitialState initialState;
    private LookUpLoanInstalmentCurrentState currentState;
    private String status;
}
