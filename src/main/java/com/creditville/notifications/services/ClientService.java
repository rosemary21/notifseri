package com.creditville.notifications.services;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.response.Client;
import com.creditville.notifications.models.response.LookUpClient;
import com.creditville.notifications.models.response.LookUpClientLoan;
import com.creditville.notifications.models.response.LookUpLoanAccount;

import java.util.List;

/**
 * Created by Chuks on 02/07/2021.
 */
public interface ClientService {
    List<Client> fetchClients() throws CustomCheckedException;

    List<Client> fetchClients(String afterExternalID) throws CustomCheckedException;

    LookUpClient lookupClient(String clientId) throws CustomCheckedException;

    LookUpLoanAccount lookupLoanAccount(String loanAccountId) throws CustomCheckedException;
}
