package com.creditville.notifications.models.response;

import com.creditville.notifications.models.requests.SearchClientsFilter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Chuks on 02/07/2021.
 */
@Getter
@Setter
public class SearchClients implements Serializable {
    private SearchClientsFilter filter;
    private List<Client> clients;
}
