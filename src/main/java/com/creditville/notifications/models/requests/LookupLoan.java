package com.creditville.notifications.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Chuks on 02/08/2021.
 */
@Getter
@Setter
@AllArgsConstructor
public class LookupLoan {
    @JsonProperty("ID")
    private String id;
}
