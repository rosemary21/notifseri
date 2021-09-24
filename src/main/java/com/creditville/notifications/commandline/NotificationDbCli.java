package com.creditville.notifications.commandline;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.Branch;
import com.creditville.notifications.models.NotificationConfig;
import com.creditville.notifications.models.NotificationType;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private NotificationConfigService notificationConfigService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("About running the notification of get allo collection officers");
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

        if(notificationConfigService.getAllNotificationGeneralConfig().isEmpty()) {
            for(NotificationType notificationType : NotificationType.values()) {
                notificationConfigService.createNewGeneralConfig(notificationType.name());
                if(
                        notificationType == NotificationType.ARREARS ||
                                notificationType == NotificationType.POST_MATURITY ||
                                notificationType == NotificationType.CHEQUE_LODGEMENT
                ) {
                    notificationConfigService.toggleGeneralConfigSwitch(notificationType.name(), "OFF");
                }
            }
        }

        if(notificationConfigService.getAllNotificationConfig().isEmpty()) {
            for (Branch branch : branchService.getAllBranches()) {
                for(NotificationType notificationType : NotificationType.values()) {
                    notificationConfigService.createNew(branch.getId(), notificationType.name());
                    if(
                            notificationType == NotificationType.ARREARS ||
                            notificationType == NotificationType.POST_MATURITY ||
                            notificationType == NotificationType.CHEQUE_LODGEMENT
                    ) {
                        notificationConfigService.toggleConfigSwitch(branch.getId(), notificationType.name(), "OFF");
                    }
                }
            }
        }

//        List<Client> customerList = new ArrayList<>();
//        Client client = new Client();
//        client.setName("David Udechukwu");
//        client.setEmail("david.udechukwu@creditville.ng");
//        customerList.add(client);
//
//        Client client2 = new Client();
//        client2.setName("David Udechukwu");
//        client2.setEmail("davidudechukwu97@gmail.com");
//        customerList.add(client2);
//
//        Client client3 = new Client();
//        client3.setName("Senami Atika");
//        client3.setEmail("senami.atika@creditville.ng");
//        customerList.add(client3);
//
//        Client client4 = new Client();
//        client4.setName("Martins Nwanu");
//        client4.setEmail("martins.nwanu@creditville.ng");
//        customerList.add(client4);
//
//        Client client5 = new Client();
//        client5.setName("Richard Rotoye");
//        client5.setEmail("richard.rotoye@creditville.ng");
//        customerList.add(client5);
//        for(Client customer : customerList) {
//            Map<String, String> notificationData = new HashMap<>();
//            notificationData.put("toName", customer.getName());
//            notificationData.put("toAddress", customer.getEmail());
//            notificationData.put("customerName", customer.getName());
//            try {
//                notificationService.sendEmailNotification("Out of Office Notification", notificationData, "email/eid_holiday");
//            } catch (CustomCheckedException cce) {
//                cce.printStackTrace();
//            }
//        }
//        System.out.println("All test mails sent...");
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

//        double result = 0;
//        double D7 = 200000;
//        double d11 = 12;
//        double D9 = 5.5;
//        result = (D7*D9/12)/Math.pow((1-(1+D9/12)),(-(d11/12)*12));

//        if (D9 < 1E-6) {
//            result = (D7 / d11);
//        }
//        result = (D7*D9) / (1.0 - Math.pow(1 + D9, -d11));


//        System.out.println("Result is: "+ this.calculatePMT(new BigDecimal("0.5"), 12, new BigDecimal("200000"), false));
    }

    private BigDecimal calculatePMT(BigDecimal rate, Integer months, BigDecimal presentValue, boolean t) {
//        double loanAmt = 100000;
//        double roi = 9.65;
//        int timePeriod = 60;

        double loanAmt = 200000;
        double roi = 0.55;
        int timePeriod = 12;

        double emi = (loanAmt * (roi/12)/100 * Math.pow((1+(roi/12)/100),timePeriod))/(Math.pow(1+(roi/12)/100, timePeriod)-1);
        return new BigDecimal(emi);
    }
}
