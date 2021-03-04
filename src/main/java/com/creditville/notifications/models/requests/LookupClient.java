package com.creditville.notifications.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Chuks on 02/08/2021.
 */
@Getter
@Setter
@AllArgsConstructor
public class LookupClient implements Serializable {
    @JsonProperty("ID")
    private String id;
}
