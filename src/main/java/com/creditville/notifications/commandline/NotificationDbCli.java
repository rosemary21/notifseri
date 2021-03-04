package com.creditville.notifications.commandline;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Chuks on 02/07/2021.
 */
@Order(1)
@Component
public class NotificationDbCli implements CommandLineRunner {
//    @Autowired
//    private ClientService clientService;

    @Override
    public void run(String... args) throws Exception {
//        try {
//            for(Client client : clientService.fetchClients()) {
//                LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
//                List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
//                        .stream()
//                        .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
//                        .collect(Collectors.toList());
////                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
//                if(!openClientLoanList.isEmpty()) {
//                    LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                    System.out.println("Open client loan is: "+ clientLoan.getId() + ". Status: "+ clientLoan.getStatus());
//                    LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
//                    List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
//                    for(LookUpLoanInstalment lookUpLoanInstalment : loanInstalments) {
//                        System.out.println("Lookup loan installment is: " + lookUpLoanInstalment.getStatus());
//                        System.out.println("Payment date is: " + lookUpLoanInstalment.getObligatoryPaymentDate());
//                    }
//                }
//            }
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//        }
    }
}
