package com.creditville.notifications.commandline;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.services.BranchService;
import com.creditville.notifications.services.ClientService;
import com.creditville.notifications.services.CollectionOfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private CollectionOfficerService collectionOfficerService;

    @Value("${app.collectionOfficer}")
    private String collectionOfficerName;

    @Value("${app.collectionPhoneNumber}")
    private String collectionPhoneNumber;

    @Value("${app.collectionEmail}")
    private String collectionOfficerEmail;

    @Autowired
    private BranchService branchService;

    @Override
    public void run(String... args) throws Exception {
        if(collectionOfficerService.getAllCollectionOfficers().isEmpty()) {
            collectionOfficerService.createNew("Abuja", "Adewonuola Adebayo", "adewonuola.adebayo@creditville.ng", "08036468906");
            collectionOfficerService.createNew("Port Harcourt", "Ruth Falade", "ruth.falade@creditville.ng", "08138808861");
            collectionOfficerService.createNew("Lagos Mainland", "Saheed Kolawole", "saheed.kolawole@creditville.ng", "08024533917");
            collectionOfficerService.createNew("Enugu", "Ruth Falade", "ruth.falade@creditville.ng", "08138808861");
            collectionOfficerService.createNew("Onitsha", "Saheed Kolawole", "saheed.kolawole@creditville.ng", "08024533917");
            collectionOfficerService.createNew("Lagos Island", "Adewonuola Adebayo", "adewonuola.adebayo@creditville.ng", "08036468906");
            collectionOfficerService.createNew("CVL_Touchgold", "Adewonuola Adebayo", "adewonuola.adebayo@creditville.ng", "08036468906");
            collectionOfficerService.createNew("Benin City", "Saheed Kolawole", "saheed.kolawole@creditville.ng", "08024533917");
            collectionOfficerService.createNew("SSU", "Adewonuola Adebayo", "adewonuola.adebayo@creditville.ng", "08036468906");
            collectionOfficerService.createNew("Head Office Branch", "Adewonuola Adebayo", "adewonuola.adebayo@creditville.ng", "08036468906");
        }

        if(!collectionOfficerService.getAllCollectionOfficers().isEmpty()) {
            if(branchService.getAllBranches().isEmpty()) {
               branchService.createBranch("Abuja");
               branchService.createBranch("Port Harcourt");
               branchService.createBranch("Lagos Mainland");
               branchService.createBranch("Enugu");
               branchService.createBranch("Enugu 2");
               branchService.createBranch("Lagos Island");
               branchService.createBranch("CVL_Touchgold");
               branchService.createBranch("Benin City");
               branchService.createBranch("SSU");
               branchService.createBranch("Head Office Branch");
            }
        }
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
