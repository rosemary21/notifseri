package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Chuks on 02/08/2021.
 */
@Getter
@Setter
public class LookUpClient implements Serializable {
    private Client client;
    private List<LookUpClientLoan> loans;
}
