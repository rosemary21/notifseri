package com.creditville.notifications.models.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Chuks on 02/07/2021.
 */
@Getter
@Setter
@AllArgsConstructor
public class SearchClients implements Serializable {
    private SearchClientsFilter filter;
    private Integer limit;
}
