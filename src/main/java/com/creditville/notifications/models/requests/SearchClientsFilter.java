package com.creditville.notifications.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Chuks on 02/07/2021.
 */
@Getter
@Setter
@NoArgsConstructor
public class SearchClientsFilter implements Serializable {
    private String modifiedFromDate;
    @JsonProperty("afterExternalID")
    private String afterExternalId;
    private String[] status;
    private String name = "";
    private String email = "";
    private String mobile = "";

    public SearchClientsFilter(String[] status) {
        this.status = status;
    }

    public SearchClientsFilter(String[] status, String afterExternalId) {
        this.status = status;
        this.afterExternalId = afterExternalId;
    }
}
