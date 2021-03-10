package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.BranchManager;
import com.creditville.notifications.models.CollectionOfficer;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.services.*;
import com.creditville.notifications.utils.CurrencyUtil;
import com.creditville.notifications.utils.DateUtil;
import com.creditville.notifications.models.CardDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Chuks on 02/09/2021.
 */
@Slf4j
@Service
public class DispatcherServiceImpl implements DispatcherService {
    @Autowired
    private ClientService clientService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DateUtil dateUtil;

    @Value("${app.useDefaultMailInfo}")
    private boolean useDefaultMailInfo;

    @Value("${app.defaultToAddress}")
    private String defaultToAddress;

    @Value("${app.defaultToName}")
    private String defaultToName;

    @Value("${mail.doRentalSubject}")
    private String doRentalSubject;

    @Value("${mail.dispatchedMailsSubject}")
    private String dispatchedMailsSubject;

    @Value("${mail.arrearsSubject}")
    private String arrearsSubject;

    @Value("${mail.postMaturitySubject}")
    private String postMaturitySubject;

    @Value("${mail.chequeLodgementSubject}")
    private String chequeLodgementSubject;

    @Value("${app.collectionOfficer}")
    private String defaultCollectionOfficer;

    @Value("${app.collectionPhoneNumber}")
    private String collectionPhoneNumber;

    @Value("${app.collectionEmail}")
    private String collectionEmail;

    @Value("${app.companyName}")
    private String companyName;

    @Value("${app.accountName}")
    private String accountName;

    @Value("${app.accountNumber}")
    private String accountNumber;

    @Value("${app.bankName}")
    private String bankName;

    @Autowired
    private EmailService emailService;
    
    @Value("${app.cheque.modeOfRepaymentKey}")
    private String chequeModeOfRepaymentKey;

    @Value("${app.card.modeOfRepaymentKey}")
    private String cardModeOfRepaymentKey;

    @Autowired
    private CardDetailsService cardDetailsService;

    @Autowired
    private CurrencyUtil currencyUtil;

    @Autowired
    private CollectionOfficerService collectionOfficerService;

    @Autowired
    private BranchManagerService branchManagerService;

    @Override
    public void performDueRentalOperation() {
        try {
            int totalMailCounter = 0;
            int failedCounter = 0;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if(!clients.isEmpty()) {
                    for(Client client : clients) {
                        CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                        BranchManager branchManager = branchManagerService.getBranchManager(client.getBranchName());
                        LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                        List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                        if(!openClientLoanList.isEmpty()) {
                            LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                            LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
//                            String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
//                                    "" :
//                                    lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
//                            if (!modeOfRepayment.equalsIgnoreCase(chequeModeOfRepaymentKey)) {
                                List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                if (!loanInstalments.isEmpty()) {
                                    List<LookUpLoanInstalment> loanInstalmentsGtOrEqToday = loanInstalments
                                            .stream()
                                            .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateGtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                            .collect(Collectors.toList());
                                    if (!loanInstalmentsGtOrEqToday.isEmpty()) {
//                                        System.out.println(">= ");
                                        List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsGtOrEqToday
                                                .stream()
                                                .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateWithinCurrentMonth(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                                .collect(Collectors.toList());
                                        LookUpLoanInstalment thisMonthInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
                                        if (thisMonthInstalment != null) {
                                            Client customer = lookUpClient.getClient();
                                            LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(thisMonthInstalment.getObligatoryPaymentDate());
                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
                                            if (!emailService.alreadySentOutEmailToday(
                                                    toAddress,
                                                    customer.getName(),
                                                    doRentalSubject,
                                                    obligatoryPaymentDate
                                            )) {
                                                Map<String, String> notificationData = new HashMap<>();
                                                String coN;
                                                String coE;
                                                String coP;
                                                if(collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                }else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                String brmN = "";
                                                String brmE = "";
                                                String brmPh = "";
                                                Boolean hasBranchManager = true;
                                                if(branchManager == null) {
                                                    hasBranchManager = false;
                                                }else {
                                                    brmN = branchManager.getOfficerName();
                                                    brmE = branchManager.getOfficerEmail();
                                                    brmPh = branchManager.getOfficerPhoneNo();
                                                }
                                                BigDecimal rentalAmount = thisMonthInstalment.getCurrentState().getPrincipalDueAmount().add(thisMonthInstalment.getCurrentState().getInterestDueAmount());
                                                notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                notificationData.put("toAddress", toAddress);
                                                notificationData.put("customerName", customer.getName());
                                                notificationData.put("paymentMonth", dateUtil.getMonthByDate(thisMonthInstalment.getObligatoryPaymentDate()));
                                                notificationData.put("paymentDate", obligatoryPaymentDate.toString());
                                                notificationData.put("paymentYear", Integer.toString(dateUtil.getYearByDate(obligatoryPaymentDate.toString())));
//                                                notificationData.put("rentalAmount", thisMonthInstalment.getCurrentState().getPrincipalDueAmount().toString());
                                                notificationData.put("rentalAmount", currencyUtil.getFormattedCurrency(rentalAmount));
                                                notificationData.put("collectionOfficer", coN);
                                                notificationData.put("collectionPhoneNumber", coP);
                                                notificationData.put("collectionEmail", coE);
                                                notificationData.put("hasBranchManager", hasBranchManager.toString());
                                                notificationData.put("branchManagerName", brmN);
                                                notificationData.put("branchManagerPhoneNumber", brmPh);
                                                notificationData.put("branchManagerEmail", brmE);
                                                notificationData.put("companyName", companyName);
                                                notificationData.put("loanId", clientLoan.getId());
                                                notificationData.put("accountName", accountName);
                                                notificationData.put("accountNumber", accountNumber);
                                                notificationData.put("bankName", bankName);
                                                totalMailCounter++;
                                                try {
                                                    notificationService.sendEmailNotification(doRentalSubject, notificationData, "email/due_rental");
                                                } catch (CustomCheckedException cce) {
                                                    cce.printStackTrace();
                                                    failedCounter++;
                                                    log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                }else {
                    lastExternalId = null;
                }
            }
            this.notifyTeamOfOperation("Due rental 1", totalMailCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performDueRentalTwoOperation() {
        try {
            int totalMailCounter = 0;
            int failedCounter = 0;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                        BranchManager branchManager = branchManagerService.getBranchManager(client.getBranchName());
                        LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                        List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                        if (!openClientLoanList.isEmpty()) {
                            LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                            LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
//                            String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
//                                    "" :
//                                    lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
//                            if (!modeOfRepayment.equalsIgnoreCase(chequeModeOfRepaymentKey)) {
                                List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                if (!loanInstalments.isEmpty()) {
                                    List<LookUpLoanInstalment> loanInstalmentsGtOrEqToday = loanInstalments
                                            .stream()
                                            .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateGtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                            .collect(Collectors.toList());
                                    if (!loanInstalmentsGtOrEqToday.isEmpty()) {
                                        List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsGtOrEqToday
                                                .stream()
                                                .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateWithinDaysNumber(lookUpLoanInstalment.getObligatoryPaymentDate(), 2))
                                                .collect(Collectors.toList());
                                        LookUpLoanInstalment fortyEightHoursInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
                                        if (fortyEightHoursInstalment != null) {
//                                            System.out.println("Forty Eight: " + fortyEightHoursInstalment.getObligatoryPaymentDate());
                                            Client customer = lookUpClient.getClient();
                                            LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(fortyEightHoursInstalment.getObligatoryPaymentDate());
                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
                                            if (!emailService.alreadySentOutEmailToday(
                                                    toAddress,
                                                    customer.getName(),
                                                    doRentalSubject,
                                                    obligatoryPaymentDate
                                            )) {
                                                Map<String, String> notificationData = new HashMap<>();
                                                String coN;
                                                String coE;
                                                String coP;
                                                if(collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                }else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                String brmN = "";
                                                String brmE = "";
                                                String brmPh = "";
                                                Boolean hasBranchManager = true;
                                                if(branchManager == null) {
                                                    hasBranchManager = false;
                                                }else {
                                                    brmN = branchManager.getOfficerName();
                                                    brmE = branchManager.getOfficerEmail();
                                                    brmPh = branchManager.getOfficerPhoneNo();
                                                }
                                                BigDecimal rentalAmount = fortyEightHoursInstalment.getCurrentState().getPrincipalDueAmount().add(fortyEightHoursInstalment.getCurrentState().getInterestDueAmount());
                                                notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                notificationData.put("toAddress", toAddress);
                                                notificationData.put("customerName", customer.getName());
                                                notificationData.put("paymentMonth", dateUtil.getMonthByDate(fortyEightHoursInstalment.getObligatoryPaymentDate()));
                                                notificationData.put("paymentDate", obligatoryPaymentDate.toString());
                                                notificationData.put("paymentYear", Integer.toString(dateUtil.getYearByDate(obligatoryPaymentDate.toString())));
//                                                notificationData.put("rentalAmount", fortyEightHoursInstalment.getCurrentState().getPrincipalDueAmount().toString());
                                                notificationData.put("rentalAmount", currencyUtil.getFormattedCurrency(rentalAmount));
                                                notificationData.put("collectionOfficer", coN);
                                                notificationData.put("collectionPhoneNumber", coP);
                                                notificationData.put("collectionEmail", coE);
                                                notificationData.put("hasBranchManager", hasBranchManager.toString());
                                                notificationData.put("branchManagerName", brmN);
                                                notificationData.put("branchManagerPhoneNumber", brmPh);
                                                notificationData.put("branchManagerEmail", brmE);
                                                notificationData.put("companyName", companyName);
                                                notificationData.put("loanId", clientLoan.getId());
                                                notificationData.put("accountName", accountName);
                                                notificationData.put("accountNumber", accountNumber);
                                                notificationData.put("bankName", bankName);
                                                totalMailCounter++;
                                                try {
                                                    notificationService.sendEmailNotification(doRentalSubject, notificationData, "email/due_rental");
                                                } catch (CustomCheckedException cce) {
                                                    cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
                                                    failedCounter++;
                                                    log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
            this.notifyTeamOfOperation("Due rental 2", totalMailCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performDueRentalThreeOperation() {
        try {
            int totalMailCounter = 0;
            int failedCounter = 0;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                        BranchManager branchManager = branchManagerService.getBranchManager(client.getBranchName());
                        LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                        List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                        if (!openClientLoanList.isEmpty()) {
                            LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                            LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
//                            String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
//                                    "" :
//                                    lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
//                            if (!modeOfRepayment.equalsIgnoreCase(chequeModeOfRepaymentKey)) {
                                List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                if (!loanInstalments.isEmpty()) {
                                    List<LookUpLoanInstalment> loanInstalmentsGtOrEqToday = loanInstalments
                                            .stream()
                                            .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateGtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                            .collect(Collectors.toList());
                                    if (!loanInstalmentsGtOrEqToday.isEmpty()) {
                                        List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsGtOrEqToday
                                                .stream()
                                                .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                                .collect(Collectors.toList());
                                        LookUpLoanInstalment todayInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
                                        if (todayInstalment != null) {
//                                            System.out.println("Forty Eight: " + todayInstalment.getObligatoryPaymentDate());
                                            Client customer = lookUpClient.getClient();
                                            LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(todayInstalment.getObligatoryPaymentDate());
                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
                                            if (!emailService.alreadySentOutEmailToday(
                                                    toAddress,
                                                    customer.getName(),
                                                    doRentalSubject,
                                                    obligatoryPaymentDate
                                            )) {
                                                Map<String, String> notificationData = new HashMap<>();
                                                String coN;
                                                String coE;
                                                String coP;
                                                if(collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                }else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                String brmN = "";
                                                String brmE = "";
                                                String brmPh = "";
                                                Boolean hasBranchManager = true;
                                                if(branchManager == null) {
                                                    hasBranchManager = false;
                                                }else {
                                                    brmN = branchManager.getOfficerName();
                                                    brmE = branchManager.getOfficerEmail();
                                                    brmPh = branchManager.getOfficerPhoneNo();
                                                }
                                                BigDecimal rentalAmount = todayInstalment.getCurrentState().getPrincipalDueAmount().add(todayInstalment.getCurrentState().getInterestDueAmount());
                                                notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                notificationData.put("toAddress", toAddress);
                                                notificationData.put("customerName", customer.getName());
                                                notificationData.put("paymentMonth", dateUtil.getMonthByDate(todayInstalment.getObligatoryPaymentDate()));
                                                notificationData.put("paymentDate", obligatoryPaymentDate.toString());
                                                notificationData.put("paymentYear", Integer.toString(dateUtil.getYearByDate(obligatoryPaymentDate.toString())));
//                                                notificationData.put("rentalAmount", todayInstalment.getCurrentState().getPrincipalDueAmount().toString());
                                                notificationData.put("rentalAmount", currencyUtil.getFormattedCurrency(rentalAmount));
                                                notificationData.put("collectionOfficer", coN);
                                                notificationData.put("collectionPhoneNumber", coP);
                                                notificationData.put("collectionEmail", coE);
                                                notificationData.put("hasBranchManager", hasBranchManager.toString());
                                                notificationData.put("branchManagerName", brmN);
                                                notificationData.put("branchManagerPhoneNumber", brmPh);
                                                notificationData.put("branchManagerEmail", brmE);
                                                notificationData.put("companyName", companyName);
                                                notificationData.put("loanId", clientLoan.getId());
                                                notificationData.put("accountName", accountName);
                                                notificationData.put("accountNumber", accountNumber);
                                                notificationData.put("bankName", bankName);
                                                totalMailCounter++;
                                                try {
                                                    notificationService.sendEmailNotification(doRentalSubject, notificationData, "email/due_rental");
                                                } catch (CustomCheckedException cce) {
                                                    cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
                                                    failedCounter++;
                                                    log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
            this.notifyTeamOfOperation("Due rental 3", totalMailCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performArrearsOperation() {
        try {
            int totalMailCounter = 0;
            int failedCounter = 0;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                        LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                        List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                        if (!openClientLoanList.isEmpty()) {
                            LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                            LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
                            List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                            if (!loanInstalments.isEmpty()) {
                                List<LookUpLoanInstalment> loanInstalmentsLtToday = loanInstalments
                                        .stream()
                                        .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateLtToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                        .collect(Collectors.toList());
                                if (!loanInstalmentsLtToday.isEmpty()) {
                                    LookUpLoanInstalment latestInstalment = loanInstalmentsLtToday.get((loanInstalmentsLtToday.size() - 1));
                                    if (latestInstalment.getCurrentState().getPrincipalDueAmount().compareTo(BigDecimal.ZERO) > 0) {
//                                    Customer is owing. Calculate arrears...
                                        List<LookUpLoanInstalment> loanInstalmentsInArrears = loanInstalmentsLtToday
                                                .stream()
                                                .filter(lookUpLoanInstalment -> (lookUpLoanInstalment.getCurrentState().getPrincipalDueAmount().compareTo(BigDecimal.ZERO) > 0))
                                                .collect(Collectors.toList());
                                        int noOfArrears = 0;
                                        BigDecimal valueOfArrears = BigDecimal.ZERO;
                                        for (LookUpLoanInstalment lookUpLoanInstalment : loanInstalmentsInArrears) {
//                                            System.out.println("Arrears: " + lookUpLoanInstalment.getObligatoryPaymentDate());
                                            LookUpLoanInstalmentCurrentState currentState = lookUpLoanInstalment.getCurrentState();
                                            valueOfArrears = valueOfArrears.add(currentState.getPrincipalDueAmount());
                                            valueOfArrears = valueOfArrears.add(currentState.getInterestDueAmount());
                                            noOfArrears++;
                                        }

                                        Client customer = lookUpClient.getClient();
                                        String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
                                        if (!emailService.alreadySentOutEmailToday(
                                                toAddress,
                                                customer.getName(),
                                                arrearsSubject,
                                                null
                                        )) {
                                            Map<String, String> notificationData = new HashMap<>();
                                            String coN;
                                            String coE;
                                            String coP;
                                            if(collectionOfficer == null) {
                                                coN = defaultCollectionOfficer;
                                                coE = collectionEmail;
                                                coP = collectionPhoneNumber;
                                            }else {
                                                coN = collectionOfficer.getOfficerName();
                                                coE = collectionOfficer.getOfficerEmail();
                                                coP = collectionOfficer.getOfficerPhoneNo();
                                            }
                                            notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                            notificationData.put("toAddress", toAddress);
                                            notificationData.put("customerName", customer.getName());
                                            notificationData.put("noOfArrears", String.valueOf(noOfArrears));
//                                            notificationData.put("valueOfArrears", valueOfArrears.toString());
                                            notificationData.put("valueOfArrears", currencyUtil.getFormattedCurrency(valueOfArrears));
                                            notificationData.put("collectionOfficer", coN);
                                            notificationData.put("collectionPhoneNumber", coP);
                                            notificationData.put("collectionEmail", coE);
                                            notificationData.put("companyName", companyName);
                                            notificationData.put("loanId", clientLoan.getId());
                                            notificationData.put("accountName", accountName);
                                            notificationData.put("accountNumber", accountNumber);
                                            notificationData.put("bankName", bankName);
                                            totalMailCounter++;
                                            try {
                                                notificationService.sendEmailNotification(arrearsSubject, notificationData, "email/arrears");
                                            } catch (CustomCheckedException cce) {
                                                cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
                                                failedCounter++;
                                                log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
            this.notifyTeamOfOperation("Arrears", totalMailCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performPostMaturityOperation() {
        try {
            int totalMailCounter = 0;
            int failedCounter = 0;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                        LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                        List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                        if (!openClientLoanList.isEmpty()) {
                            LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                            LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
                            List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                            if (!loanInstalments.isEmpty()) {
                                List<LookUpLoanInstalment> loanInstalmentsGtToday = loanInstalments
                                        .stream()
                                        .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateGtToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                        .collect(Collectors.toList());
                                if (loanInstalmentsGtToday.isEmpty()) {
                                    List<LookUpLoanInstalment> loanInstalmentsLtOrEqToday = loanInstalments
                                            .stream()
                                            .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateLtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                            .collect(Collectors.toList());
                                    if (!loanInstalmentsLtOrEqToday.isEmpty()) {
                                        LookUpLoanInstalment latestInstalment = loanInstalmentsLtOrEqToday.get((loanInstalmentsLtOrEqToday.size() - 1));
                                        if (latestInstalment.getCurrentState().getPrincipalDueAmount().compareTo(BigDecimal.ZERO) > 0) {
//                                    Customer is owing but maturity date exceeded...
                                            Client customer = lookUpClient.getClient();
                                            LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(latestInstalment.getObligatoryPaymentDate());
                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
                                            if (!emailService.alreadySentOutEmailToday(
                                                    toAddress,
                                                    customer.getName(),
                                                    arrearsSubject,
                                                    obligatoryPaymentDate
                                            )) {
                                                Map<String, String> notificationData = new HashMap<>();
                                                String coN;
                                                String coE;
                                                String coP;
                                                if(collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                }else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                BigDecimal outstandingBalance = latestInstalment.getCurrentState().getPrincipalDueAmount().add(latestInstalment.getCurrentState().getInterestDueAmount());
                                                notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                notificationData.put("toAddress", toAddress);
                                                notificationData.put("customerName", customer.getName());
                                                notificationData.put("maturityDate", dateUtil.convertDateToLocalDate(latestInstalment.getObligatoryPaymentDate()).toString());
//                                                notificationData.put("outstandingBalance", latestInstalment.getCurrentState().getPrincipalDueAmount().toString());
                                                notificationData.put("outstandingBalance", currencyUtil.getFormattedCurrency(outstandingBalance));
                                                notificationData.put("collectionOfficer", coN);
                                                notificationData.put("collectionPhoneNumber", coP);
                                                notificationData.put("collectionEmail", coE);
                                                notificationData.put("companyName", companyName);
                                                notificationData.put("loanId", clientLoan.getId());
                                                notificationData.put("accountName", accountName);
                                                notificationData.put("accountNumber", accountNumber);
                                                notificationData.put("bankName", bankName);
                                                totalMailCounter++;
                                                try {
                                                    notificationService.sendEmailNotification(postMaturitySubject, notificationData, "email/post_maturity");
                                                } catch (CustomCheckedException cce) {
                                                    cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
                                                    failedCounter++;
                                                    log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
            this.notifyTeamOfOperation("Post Maturity", totalMailCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performChequeLodgementOperation() {
        try {
            int totalMailCounter = 0;
            int failedCounter = 0;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                        LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                        List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                        if (!openClientLoanList.isEmpty()) {
                            LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                            LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
                            String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
                                    "" :
                                    lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
                            if (modeOfRepayment.equalsIgnoreCase(chequeModeOfRepaymentKey)) {
                                List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                if (!loanInstalments.isEmpty()) {
                                    List<LookUpLoanInstalment> loanInstalmentsGtOrEqToday = loanInstalments
                                            .stream()
                                            .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateGtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                            .collect(Collectors.toList());
                                    if (!loanInstalmentsGtOrEqToday.isEmpty()) {
                                        List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsGtOrEqToday
                                                .stream()
                                                .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateWithinDaysNumber(lookUpLoanInstalment.getObligatoryPaymentDate(), 7))
                                                .collect(Collectors.toList());
                                        LookUpLoanInstalment thisMonthInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
                                        if (thisMonthInstalment != null) {
                                            Client customer = lookUpClient.getClient();
                                            LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(thisMonthInstalment.getObligatoryPaymentDate());
                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
                                            if (!emailService.alreadySentOutEmailToday(
                                                    toAddress,
                                                    customer.getName(),
                                                    doRentalSubject,
                                                    obligatoryPaymentDate
                                            )) {
                                                Map<String, String> notificationData = new HashMap<>();
                                                String coN;
                                                String coE;
                                                String coP;
                                                if(collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                }else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                BigDecimal rentalAmount = thisMonthInstalment.getCurrentState().getPrincipalDueAmount().add(thisMonthInstalment.getCurrentState().getInterestDueAmount());
                                                notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                notificationData.put("toAddress", toAddress);
                                                notificationData.put("customerName", customer.getName());
                                                notificationData.put("paymentMonth", dateUtil.getMonthByDate(thisMonthInstalment.getObligatoryPaymentDate()));
                                                notificationData.put("paymentDate", obligatoryPaymentDate.toString());
//                                                notificationData.put("rentalAmount", thisMonthInstalment.getCurrentState().getPrincipalDueAmount().toString());
                                                notificationData.put("rentalAmount", currencyUtil.getFormattedCurrency(rentalAmount));
                                                notificationData.put("collectionOfficer", coN);
                                                notificationData.put("collectionPhoneNumber", coP);
                                                notificationData.put("collectionEmail", coE);
                                                notificationData.put("companyName", companyName);
                                                notificationData.put("loanId", clientLoan.getId());
                                                notificationData.put("accountName", accountName);
                                                notificationData.put("accountNumber", accountNumber);
                                                notificationData.put("bankName", bankName);
                                                totalMailCounter++;
                                                try {
                                                    notificationService.sendEmailNotification(chequeLodgementSubject, notificationData, "email/cheque_lodgement");
                                                } catch (CustomCheckedException cce) {
                                                    cce.printStackTrace();
                                                    failedCounter++;
                                                    log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
            this.notifyTeamOfOperation("Cheque Lodgement", totalMailCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

//    @Override
//    public void performRecurringChargesOperation() {
//        try {
//            String lastExternalId = "";
//            while (lastExternalId != null) {
//                List<Client> clients = clientService.fetchClients(lastExternalId);
//                if (!clients.isEmpty()) {
//                    for (Client client : clients) {
//                        LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
//                        List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
//                                .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
//                                .collect(Collectors.toList());
////                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
//                        if (!openClientLoanList.isEmpty()) {
//                            LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
//                            LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
//                            String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
//                                    "" :
//                                    lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
//                            if (modeOfRepayment.equalsIgnoreCase(cardModeOfRepaymentKey)) {
//                                List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
//                                if (!loanInstalments.isEmpty()) {
//                                    List<LookUpLoanInstalment> loanInstalmentsGtOrEqToday = loanInstalments
//                                            .stream()
//                                            .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateGtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
//                                            .collect(Collectors.toList());
//                                    if (!loanInstalmentsGtOrEqToday.isEmpty()) {
//                                        List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsGtOrEqToday
//                                                .stream()
//                                                .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
//                                                .collect(Collectors.toList());
//                                        LookUpLoanInstalment dueDateInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
//                                        if (dueDateInstalment != null) {
//                                            Client customer = lookUpClient.getClient();
//                                            LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(dueDateInstalment.getObligatoryPaymentDate());
//                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
////                                            Perform recurring charge...
//                                            cardDetailsService.cardRecurringCharges(toAddress, dueDateInstalment.getCurrentState().getPrincipalDueAmount(), clientLoan.getId(), obligatoryPaymentDate);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    Client lastClient = clients.get((clients.size() - 1));
//                    lastExternalId = lastClient.getExternalID();
//                } else {
//                    lastExternalId = null;
//                }
//            }
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//        }
//    }

    @Override
    public void performRecurringChargesOperation() {
        try {
            Integer pageNumber = 0;

            while (pageNumber != null) {
                List<CardDetails> tokenizedCardDetails = cardDetailsService.getAllCardDetails(pageNumber, 100);
                if (!tokenizedCardDetails.isEmpty()) {
                    for (CardDetails cardDetails : tokenizedCardDetails) {
                        LookUpClient lookUpClient = clientService.lookupClient(cardDetails.getClientId());
                        String clientStatus = lookUpClient.getClient().getClientStatus();
                        if(clientStatus.equals("ACTIVE") || clientStatus.equals("IN_ARREARS")) {
                            List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                    .stream()
//                                    .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                    .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                    .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                            if (!openClientLoanList.isEmpty()) {
                                LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                                System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                                LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
                                String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
                                        "" :
                                        lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
                                if (modeOfRepayment.equalsIgnoreCase(cardModeOfRepaymentKey)) {
                                    List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                    if (!loanInstalments.isEmpty()) {
                                        List<LookUpLoanInstalment> loanInstalmentsGtOrEqToday = loanInstalments
                                                .stream()
                                                .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateGtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                                .collect(Collectors.toList());
                                        if (!loanInstalmentsGtOrEqToday.isEmpty()) {
                                            List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsGtOrEqToday
                                                    .stream()
                                                    .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                                    .collect(Collectors.toList());
                                            LookUpLoanInstalment dueDateInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
//                                            System.out.println("Loan due date installment is: " + dueDateInstalment);
                                            if (dueDateInstalment != null) {
//                                                System.out.println("Loan due date installment date is: " + dueDateInstalment.getObligatoryPaymentDate());
                                                Client customer = lookUpClient.getClient();
                                                LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(dueDateInstalment.getObligatoryPaymentDate());
                                                String toAddress = customer.getEmail();
                                                BigDecimal totalDue = dueDateInstalment.getCurrentState().getPrincipalDueAmount()
                                                        .add(dueDateInstalment.getCurrentState().getInterestDueAmount());
//                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
//                                            Perform recurring charge...
                                                cardDetailsService.cardRecurringCharges(toAddress, totalDue, clientLoan.getId(), obligatoryPaymentDate, customer.getExternalID());
                                            }
                                        } else System.out.println("Loan installments gt or eq to today is empty...");
                                    } else System.out.println("Loan installments is empty...");
                                } else System.out.println("Mode of repayment is not known..." + modeOfRepayment);
                            }
                        }
                    }
                    pageNumber++;
                } else {
                    pageNumber = null;
                }
            }
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    private void notifyTeamOfOperation(String operationName, int totalMailCounter, int failedCounter) throws CustomCheckedException {
        if(totalMailCounter > 0) {
            int totalSuccessfulCount = (totalMailCounter - failedCounter);
            Map<String, String> notificationData = new HashMap<>();
            notificationData.put("toAddress", defaultToAddress);
            notificationData.put("toName", defaultToName);
            notificationData.put("totalSuccessfulCount", String.valueOf(totalSuccessfulCount));
            notificationData.put("totalFailedCount", String.valueOf(failedCounter));
            notificationData.put("operationName", operationName);
            notificationService.sendEmailNotification(dispatchedMailsSubject, notificationData, "email/dispatched_mails");
        }
    }
}
