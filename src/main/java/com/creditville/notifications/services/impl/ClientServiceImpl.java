package com.creditville.notifications.services.impl;

import com.creditville.notifications.executor.HttpCallService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.JsonNode;
import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.requests.LookupClient;
import com.creditville.notifications.models.requests.LookupLoan;
import com.creditville.notifications.models.requests.SearchClients;
import com.creditville.notifications.models.requests.SearchClientsFilter;
import com.creditville.notifications.models.response.Client;
import com.creditville.notifications.models.response.LookUpClient;
import com.creditville.notifications.models.response.LookUpClientLoan;
import com.creditville.notifications.models.response.LookUpLoanAccount;
import com.creditville.notifications.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Chuks on 02/07/2021.
 */
@Service
public class ClientServiceImpl implements ClientService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpCallService httpCallService;

    @Value("${instafin.base.url}")
    private String baseUrl;

    @Value("${instafin.client.searchUrl}")
    private String searchClientsUrl;

    @Value("${instafin.get.client.url}")
    private String lookupClientUrl;

    @Value("${instafin.loan.lookUpUrl}")
    private String lookupLoanUrl;

    @Override
    public List<Client> fetchClients() throws CustomCheckedException {
        SearchClients searchClients = this.getSearchClientsRequest(null);
        try {
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(searchClients);
//            System.out.println("Payload: "+ payload);
            String searchClientsResp = httpCallService.doBasicPost((baseUrl + searchClientsUrl), payload);
//            System.out.println("Search clients response : "+ searchClientsResp);
            com.creditville.notifications.models.response.SearchClients searchClientsResponse = objectMapper.readValue(searchClientsResp, com.creditville.notifications.models.response.SearchClients.class);
            return searchClientsResponse.getClients();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new CustomCheckedException("Unable to process search clients request, error reads: "+ e.getMessage());
        }
    }

    @Override
    public List<Client> fetchClients(String afterExternalID) throws CustomCheckedException {
        SearchClients searchClients = this.getSearchClientsRequest(afterExternalID);
        try {
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(searchClients);
//            System.out.println("Payload: "+ payload);
            String searchClientsResp = httpCallService.doBasicPost((baseUrl + searchClientsUrl), payload);
//            System.out.println("Search clients response : "+ searchClientsResp);
            com.creditville.notifications.models.response.SearchClients searchClientsResponse = objectMapper.readValue(searchClientsResp, com.creditville.notifications.models.response.SearchClients.class);
            return searchClientsResponse.getClients();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new CustomCheckedException("Unable to process search clients request, error reads: "+ e.getMessage());
        }
    }

    private SearchClients getSearchClientsRequest(String afterExternalId) {
        if(afterExternalId == null) return new SearchClients(new SearchClientsFilter(new String[] {"ACTIVE", "ARREARS"}), 100);
        else {
            if(afterExternalId.trim().equals("")) return new SearchClients(new SearchClientsFilter(new String[] {"ACTIVE", "ARREARS"}), 100);
            else return new SearchClients(new SearchClientsFilter(new String[] {"ACTIVE", "ARREARS"}, afterExternalId), 100);
        }
    }

    @Override
    public LookUpClient lookupClient(String clientId) throws CustomCheckedException {
        LookupClient lookupClient = new LookupClient(clientId);
        try {
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(lookupClient);
//            System.out.println("Payload: "+ payload);
            String lookUpClientResp = httpCallService.doBasicPost((baseUrl + lookupClientUrl), payload);
//            System.out.println("Lookup client response : "+ lookUpClientResp);
            return objectMapper.readValue(lookUpClientResp, LookUpClient.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new CustomCheckedException("Unable to process lookup client request, error reads: "+ e.getMessage());
        }
    }

    @Override
    public LookUpLoanAccount lookupLoanAccount(String loanAccountId) throws CustomCheckedException {
        LookupLoan lookupLoan = new LookupLoan(loanAccountId);
        try {
            String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(lookupLoan);
//            System.out.println("Payload: "+ payload);
            String lookUpLoanAccountResp = httpCallService.doBasicPost((baseUrl + lookupLoanUrl), payload);
//            System.out.println("Lookup loan account response : "+ lookUpLoanAccountResp);
            com.creditville.notifications.models.response.LookupLoan lookUpLoanResponse = objectMapper.readValue(lookUpLoanAccountResp, com.creditville.notifications.models.response.LookupLoan.class);
            return lookUpLoanResponse.getAccount();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new CustomCheckedException("Unable to process lookup loan request, error reads: "+ e.getMessage());
        }
    }
}
