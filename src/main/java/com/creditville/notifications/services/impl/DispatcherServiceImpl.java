package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.*;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.services.*;
import com.creditville.notifications.utils.CurrencyUtil;
import com.creditville.notifications.utils.DateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private FinanceManagerService financeManagerService;

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

    @Value("${instafin.status.arrears}")
    private String arrearStatus;

    @Value("${instafin.status.active}")
    private String activeStatus;


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

    @Value("${app.remitta.modeOfRepaymentKey}")
    private String remitaModeOfRepaymentKey;

    @Autowired
    private CardDetailsService cardDetailsService;

    @Autowired
    private CurrencyUtil currencyUtil;

    @Autowired
    private CollectionOfficerService collectionOfficerService;

    @Autowired
    private BranchManagerService branchManagerService;

    @Autowired
    private RecoveryOfficerService recoveryOfficerService;

    @Autowired
    private MailMonitorService mailMonitorService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private NotificationConfigService notificationConfigService;
    @Autowired
    private ObjectMapper om;




    @Autowired
    private RemitaService remitaService;

    @Override
    public void performDueRentalOperation() {
        try {
            Long totalSuccessfulCounter = 0L;
            Long failedCounter = 0L;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if(!clients.isEmpty()) {
                    for(Client client : clients) {
                        try {
                            CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                            Branch branch = branchService.getBranch(client.getBranchName());
                            if(!branch.getIsEnabled()) {
                                log.info("Branch {} is disabled from receiving notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                            }
                            NotificationConfig notificationConfig = notificationConfigService.getNotificationConfig(branch.getId(), NotificationType.DUE_RENTAL_ONE.name());
                            if(notificationConfig != null) {
                                if(!notificationConfig.getIsEnabled()) {
                                    log.info("Branch {} is disabled from receiving due rental 1 notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                    throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                                }
                            }
                            System.out.println("getting the branch <><><><> "+client.getBranchName());
                            BranchManager branchManager = branchManagerService.getBranchManager(client.getBranchName());
                            LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                            List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                    .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                    .filter(cl -> cl.getStatus().equalsIgnoreCase(activeStatus) || cl.getStatus().contains(arrearStatus))
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
//                                        System.out.println(">= ");
                                        List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsGtOrEqToday
                                                .stream()
                                                .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateWithinCurrentMonth(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                                .collect(Collectors.toList());
                                        LookUpLoanInstalment thisMonthInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
                                        if (thisMonthInstalment != null) {
                                            Client customer = lookUpClient.getClient();
                                            LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(thisMonthInstalment.getObligatoryPaymentDate());
                                            String valueDate=dateUtil.convertDateToYear(obligatoryPaymentDate);
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
                                                if (collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                } else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                String brmN = "";
                                                String brmE = "";
                                                String brmPh = "";
                                                Boolean hasBranchManager = true;
                                                if (branchManager == null) {
                                                    hasBranchManager = false;
                                                } else {
                                                    brmN = branchManager.getOfficerName();
                                                    System.out.println("getting the branch manager"+brmN);
                                                    brmE = branchManager.getOfficerEmail();
                                                    brmPh = branchManager.getOfficerPhoneNo();
                                                }
                                                BigDecimal rentalAmount = thisMonthInstalment.getCurrentState().getPrincipalDueAmount().add(thisMonthInstalment.getCurrentState().getInterestDueAmount());
                                                if(thisMonthInstalment.getCurrentState().getFeeDueAmount() != null)
                                                    rentalAmount = rentalAmount.add(thisMonthInstalment.getCurrentState().getFeeDueAmount());
                                                if(rentalAmount.compareTo(BigDecimal.ZERO) > 0) {
                                                    notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                    notificationData.put("toAddress", toAddress);
                                                    notificationData.put("customerName", customer.getName());
                                                    notificationData.put("paymentMonth", dateUtil.getMonthByDate(thisMonthInstalment.getObligatoryPaymentDate()));
                                                    notificationData.put("paymentDate", valueDate);
                                                    notificationData.put("paymentYear", Integer.toString(dateUtil.getYearByDate(thisMonthInstalment.getObligatoryPaymentDate())));
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
                                                    totalSuccessfulCounter++;
                                                    try {
                                                        notificationService.sendEmailNotification(doRentalSubject, notificationData, "email/due_rental");
                                                    } catch (CustomCheckedException cce) {
                                                        cce.printStackTrace();
//                                                    failedCounter++;
                                                        if (!emailService.emailAlreadyFailed(obligatoryPaymentDate, toAddress, doRentalSubject)) {
                                                            failedCounter++;
                                                        }
                                                        log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
//                            failedCounter++;
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                }else {
                    lastExternalId = null;
                }
            }
//            this.notifyTeamOfOperation("Due rental 1", totalSuccessfulCounter, failedCounter);
            this.logDispatchOperation("Due rental 1", totalSuccessfulCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performDueRentalTwoOperation() {
        try {
            Long totalSuccessfulCounter = 0L;
            Long failedCounter = 0L;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        try {
                            CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                            Branch branch = branchService.getBranch(client.getBranchName());
                            if(!branch.getIsEnabled()) {
                                log.info("Branch {} is disabled from receiving notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                            }
                            NotificationConfig notificationConfig = notificationConfigService.getNotificationConfig(branch.getId(), NotificationType.DUE_RENTAL_TWO.name());
                            if(notificationConfig != null) {
                                if(!notificationConfig.getIsEnabled()) {
                                    log.info("Branch {} is disabled from receiving due rental 2 notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                    throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                                }
                            }
                            BranchManager branchManager = branchManagerService.getBranchManager(client.getBranchName());
                            LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                            List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                    .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                    .filter(cl -> cl.getStatus().equalsIgnoreCase(activeStatus) || cl.getStatus().contains(arrearStatus))
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
                                                if (collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                } else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                String brmN = "";
                                                String brmE = "";
                                                String brmPh = "";
                                                Boolean hasBranchManager = true;
                                                if (branchManager == null) {
                                                    hasBranchManager = false;
                                                } else {
                                                    brmN = branchManager.getOfficerName();
                                                    brmE = branchManager.getOfficerEmail();
                                                    brmPh = branchManager.getOfficerPhoneNo();
                                                }
                                                BigDecimal rentalAmount = fortyEightHoursInstalment.getCurrentState().getPrincipalDueAmount().add(fortyEightHoursInstalment.getCurrentState().getInterestDueAmount());
                                                if(fortyEightHoursInstalment.getCurrentState().getFeeDueAmount() != null)
                                                    rentalAmount = rentalAmount.add(fortyEightHoursInstalment.getCurrentState().getFeeDueAmount());
                                                if(rentalAmount.compareTo(BigDecimal.ZERO) > 0) {
                                                    notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                    notificationData.put("toAddress", toAddress);
                                                    notificationData.put("customerName", customer.getName());
                                                    notificationData.put("paymentMonth", dateUtil.getMonthByDate(fortyEightHoursInstalment.getObligatoryPaymentDate()));
                                                    notificationData.put("paymentDate", obligatoryPaymentDate.toString());
                                                    notificationData.put("paymentYear", Integer.toString(dateUtil.getYearByDate(fortyEightHoursInstalment.getObligatoryPaymentDate())));
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
                                                    totalSuccessfulCounter++;
                                                    try {
                                                        notificationService.sendEmailNotification(doRentalSubject, notificationData, "email/due_rental");
                                                    } catch (CustomCheckedException cce) {
                                                        cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
//                                                    failedCounter++;
                                                        if (!emailService.emailAlreadyFailed(obligatoryPaymentDate, toAddress, doRentalSubject)) {
                                                            failedCounter++;
                                                        }
                                                        log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
//                            failedCounter++;
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
//            this.notifyTeamOfOperation("Due rental 2", totalSuccessfulCounter, failedCounter);
            this.logDispatchOperation("Due rental 2", totalSuccessfulCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performDueRentalThreeOperation() {
        try {
            Long totalSuccessfulCounter = 0L;
            Long failedCounter = 0L;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        try {
                            CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                            Branch branch = branchService.getBranch(client.getBranchName());
                            if(!branch.getIsEnabled()) {
                                log.info("Branch {} is disabled from receiving notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                            }
                            NotificationConfig notificationConfig = notificationConfigService.getNotificationConfig(branch.getId(), NotificationType.DUE_RENTAL_THREE.name());
                            if(notificationConfig != null) {
                                if(!notificationConfig.getIsEnabled()) {
                                    log.info("Branch {} is disabled from receiving due rental 3 notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                    throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                                }
                            }
                            BranchManager branchManager = branchManagerService.getBranchManager(client.getBranchName());
                            LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                            List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                    .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                    .filter(cl -> cl.getStatus().equalsIgnoreCase(activeStatus) || cl.getStatus().contains(arrearStatus))
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
                                                if (collectionOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                } else {
                                                    coN = collectionOfficer.getOfficerName();
                                                    coE = collectionOfficer.getOfficerEmail();
                                                    coP = collectionOfficer.getOfficerPhoneNo();
                                                }
                                                String brmN = "";
                                                String brmE = "";
                                                String brmPh = "";
                                                Boolean hasBranchManager = true;
                                                if (branchManager == null) {
                                                    hasBranchManager = false;
                                                } else {
                                                    brmN = branchManager.getOfficerName();
                                                    brmE = branchManager.getOfficerEmail();
                                                    brmPh = branchManager.getOfficerPhoneNo();
                                                }
                                                BigDecimal rentalAmount = todayInstalment.getCurrentState().getPrincipalDueAmount().add(todayInstalment.getCurrentState().getInterestDueAmount());
                                                if(todayInstalment.getCurrentState().getFeeDueAmount() != null)
                                                    rentalAmount = rentalAmount.add(todayInstalment.getCurrentState().getFeeDueAmount());
                                                if(rentalAmount.compareTo(BigDecimal.ZERO) > 0) {
                                                    notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                    notificationData.put("toAddress", toAddress);
                                                    notificationData.put("customerName", customer.getName());
                                                    notificationData.put("paymentMonth", dateUtil.getMonthByDate(todayInstalment.getObligatoryPaymentDate()));
                                                    notificationData.put("paymentDate", obligatoryPaymentDate.toString());
                                                    notificationData.put("paymentYear", Integer.toString(dateUtil.getYearByDate(todayInstalment.getObligatoryPaymentDate())));
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
                                                    totalSuccessfulCounter++;
                                                    try {
                                                        notificationService.sendEmailNotification(doRentalSubject, notificationData, "email/due_rental");
                                                    } catch (CustomCheckedException cce) {
                                                        cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
//                                                    failedCounter++;
                                                        if (!emailService.emailAlreadyFailed(obligatoryPaymentDate, toAddress, doRentalSubject)) {
                                                            failedCounter++;
                                                        }
                                                        log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
//                            }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
//                            failedCounter++;
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
//            this.notifyTeamOfOperation("Due rental 3", totalSuccessfulCounter, failedCounter);
            this.logDispatchOperation("Due rental 3", totalSuccessfulCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performArrearsOperation() {
        try {
            Long totalSuccessfulCounter = 0L;
            Long failedCounter = 0L;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        try {
//                            CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                            RecoveryOfficer recoveryOfficer = recoveryOfficerService.getRecoveryOfficer(client.getBranchName());
                            System.out.println("getting the branch name "+client.getBranchName());
                            FinanceManager financeManager=financeManagerService.getBraManager(client.getBranchName());
                            Branch branch = branchService.getBranch(client.getBranchName());
                            if(!branch.getIsEnabled()) {
                                log.info("Branch {} is disabled from receiving notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                            }
                            NotificationConfig notificationConfig = notificationConfigService.getNotificationConfig(branch.getId(), NotificationType.ARREARS.name());
                            if(notificationConfig != null) {
                                if(!notificationConfig.getIsEnabled()) {
                                    log.info("Branch {} is disabled from receiving arrears notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                    throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                                }
                            }
                            System.out.println("getting the client value"+client.getBranchName());
                            BranchManager branchManager = branchManagerService.getBranchManager(client.getBranchName());
                            LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                            List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                    .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                    .filter(cl -> cl.getStatus().equalsIgnoreCase(activeStatus) || cl.getStatus().contains(arrearStatus))
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
                                                if(lookUpLoanInstalment.getCurrentState().getFeeDueAmount() != null)
                                                    valueOfArrears = valueOfArrears.add(lookUpLoanInstalment.getCurrentState().getFeeDueAmount());
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
//                                                if (collectionOfficer == null) {
//                                                    coN = defaultCollectionOfficer;
//                                                    coE = collectionEmail;
//                                                    coP = collectionPhoneNumber;
//                                                } else {
//                                                    coN = collectionOfficer.getOfficerName();
//                                                    coE = collectionOfficer.getOfficerEmail();
//                                                    coP = collectionOfficer.getOfficerPhoneNo();
//                                                }
                                                if (recoveryOfficer == null) {
                                                    coN = defaultCollectionOfficer;
                                                    coE = collectionEmail;
                                                    coP = collectionPhoneNumber;
                                                } else {
                                                    coN = recoveryOfficer.getOfficerName();
                                                    coE = recoveryOfficer.getOfficerEmail();
                                                    coP = recoveryOfficer.getOfficerPhoneNo();
                                                }
                                                String brmN = "";
                                                String brmE = "";
                                                String brmPh = "";
                                                String finEm="";
                                                String finNam="";
                                                String finPh="";
                                                Boolean hasBranchManager = true;
                                                if (branchManager == null) {
                                                    hasBranchManager = false;
                                                } else {
                                                    brmN = branchManager.getOfficerName();
                                                    brmE = branchManager.getOfficerEmail();
                                                    brmPh = branchManager.getOfficerPhoneNo();
                                                }
                                                finEm=financeManager.getOfficerEmail();
                                                finNam=financeManager.getOfficerName();
                                                finPh=financeManager.getOfficerPhoneNo();
                                                if(valueOfArrears.compareTo(BigDecimal.ZERO) > 0) {
                                                    notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                    notificationData.put("toAddress", toAddress);
                                                    notificationData.put("customerName", customer.getName());
                                                    notificationData.put("noOfArrears", String.valueOf(noOfArrears));
//                                                  notificationData.put("valueOfArrears", valueOfArrears.toString());
                                                    notificationData.put("valueOfArrears", currencyUtil.getFormattedCurrency(valueOfArrears));
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
                                                    notificationData.put("recoveryOfficer", coN);
                                                    notificationData.put("PhoneNo", coP);
                                                    notificationData.put("recoveryEmail", coE);
                                                    notificationData.put("branchManager", brmN);
                                                    notificationData.put("bMPhoneNo", brmPh);
                                                    notificationData.put("bMEmail", brmE);
                                                    notificationData.put("financeEmail",finEm);
                                                    notificationData.put("financeName",finNam);
                                                    notificationData.put("financePhoneNo",finPh);


                                                    totalSuccessfulCounter++;
                                                    try {
                                                        notificationService.sendEmailNotification(arrearsSubject, notificationData, "email/arrears");
                                                    } catch (CustomCheckedException cce) {
                                                        cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
//                                                    failedCounter++;
                                                        if (!emailService.emailAlreadyFailed(null, toAddress, doRentalSubject)) {
                                                            failedCounter++;
                                                        }
                                                        log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
//                            failedCounter++;
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
//            this.notifyTeamOfOperation("Arrears", totalSuccessfulCounter, failedCounter);
            this.logDispatchOperation("Arrears", totalSuccessfulCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performPostMaturityOperation() {
        try {
            Long totalSuccessfulCounter = 0L;
            Long failedCounter = 0L;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        try {
                            CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                            System.out.println("getting the branch name "+client.getBranchName());
                            RecoveryOfficer recoveryOfficer=recoveryOfficerService.getRecoveryOfficer(client.getBranchName());
                            BranchManager branchManager=branchManagerService.getBranchManager(client.getBranchName());
                            FinanceManager financeManager=financeManagerService.getBraManager(client.getBranchName());
                            log.info("getting the branch name {}",client.getBranchName());
                            Branch branch = branchService.getBranch(client.getBranchName());
                            if(!branch.getIsEnabled()) {
                                log.info("Branch {} is disabled from receiving notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                            }
                            NotificationConfig notificationConfig = notificationConfigService.getNotificationConfig(branch.getId(), NotificationType.POST_MATURITY.name());
                            if(notificationConfig != null) {
                                if(!notificationConfig.getIsEnabled()) {
                                    log.info("Branch {} is disabled from receiving post maturity notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                    throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                                }
                            }
                            LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
//                            LookUpClient lookUpClient = clientService.lookupClient("0000001006");
                            List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                    .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                    .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().contains("ARREARS"))
                                    .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                            if (!openClientLoanList.isEmpty()) {
                                LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                            System.out.println("Open client loan is: " + clientLoan.getId() + ". Status: " + clientLoan.getStatus());
                                LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
//                                LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount("1000001006");
                                System.out.println("lookUpLoanAccount init: "+om.writerWithDefaultPrettyPrinter().writeValueAsString(lookUpLoanAccount));
                                List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                System.out.println("loanInstalments is: "+om.writerWithDefaultPrettyPrinter().writeValueAsString(loanInstalments));
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
                                        System.out.println("loanInstalmentsLtOrEqToday is: "+om.writerWithDefaultPrettyPrinter().writeValueAsString(loanInstalmentsLtOrEqToday));
                                        if (!loanInstalmentsLtOrEqToday.isEmpty()) {
                                            LookUpLoanInstalment latestInstalment = loanInstalmentsLtOrEqToday.get((loanInstalmentsLtOrEqToday.size() - 1));
                                            System.out.println("latestInstalment is.: "+om.writerWithDefaultPrettyPrinter().writeValueAsString(latestInstalment));
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
                                                    String roN="";
                                                    String roE="";
                                                    String roP="";
                                                    String boN="";
                                                    String boE="";
                                                    String boP="";
                                                    String FiN="";
                                                    String FiE="";
                                                    String FiP="";
                                                    if (collectionOfficer == null) {
                                                        coN = defaultCollectionOfficer;
                                                        coE = collectionEmail;
                                                        coP = collectionPhoneNumber;
                                                    } else {
                                                        coN = collectionOfficer.getOfficerName();
                                                        coE = collectionOfficer.getOfficerEmail();
                                                        coP = collectionOfficer.getOfficerPhoneNo();
                                                    }
                                                    System.out.println("Getting the recovery officer "+recoveryOfficer);
                                                    System.out.println("Getting the branch manager "+branchManager);
                                                    if(recoveryOfficer!=null){
                                                        roN=recoveryOfficer.getOfficerName();
                                                        roE=recoveryOfficer.getOfficerEmail();
                                                        roP=recoveryOfficer.getOfficerPhoneNo();

                                                    }
                                                    if(branchManager!=null){
                                                        boN=branchManager.getOfficerName();
                                                        boE=branchManager.getOfficerEmail();
                                                        boP=branchManager.getOfficerPhoneNo();
                                                    }
                                                    if(financeManager!=null){
                                                        FiN=financeManager.getOfficerName();
                                                        FiE=financeManager.getOfficerEmail();
                                                        FiP=financeManager.getOfficerPhoneNo();
                                                    }

                                                    BigDecimal outstandingBalance = latestInstalment.getCurrentState().getPrincipalDueAmount().add(latestInstalment.getCurrentState().getInterestDueAmount());
                                                    if(latestInstalment.getCurrentState().getFeeDueAmount() != null)
                                                        outstandingBalance = outstandingBalance.add(latestInstalment.getCurrentState().getFeeDueAmount());
                                                    if(outstandingBalance.compareTo(BigDecimal.ZERO) > 0) {
                                                        notificationData.put("toName", useDefaultMailInfo ? defaultToName : customer.getName());
                                                        notificationData.put("toAddress", toAddress);
                                                        notificationData.put("customerName", customer.getName());
                                                        notificationData.put("maturityDate", dateUtil.convertLocalDateToString(latestInstalment.getObligatoryPaymentDate()));
//                                                notificationData.put("outstandingBalance", latestInstalment.getCurrentState().getPrincipalDueAmount().toString());
                                                        System.out.println("Getting the outstanding balance {}"+outstandingBalance);
//                                                        notificationData.put("outstandingBalance", currencyUtil.getFormattedCurrency(outstandingBalance));
                                                        notificationData.put("outstandingBalance",currencyUtil.getFormattedCurrency(new BigDecimal(lookUpLoanAccount.getLoanAccount().getMaximumRepayableAsOfToday())));
                                                        notificationData.put("collectionOfficer", coN);
                                                        notificationData.put("collectionPhoneNumber", coP);
                                                        notificationData.put("collectionEmail", coE);
                                                        notificationData.put("branchEmail", boE);
                                                        notificationData.put("branchPhoneNumber", boP);
                                                        notificationData.put("branchManager", boN);
                                                        notificationData.put("financeName",FiN);
                                                        notificationData.put("financePhoneNo", FiP);
                                                        notificationData.put("financeEmail", FiE);
                                                        notificationData.put("recoveryEmail", roE);
                                                        notificationData.put("recoveryPhoneNumber", roP);
                                                        notificationData.put("recoveryOfficer", roN);
                                                        notificationData.put("branchEmail", coE);
                                                        notificationData.put("companyName", companyName);
                                                        notificationData.put("loanId", clientLoan.getId());
                                                        notificationData.put("accountName", accountName);
                                                        notificationData.put("accountNumber", accountNumber);
                                                        notificationData.put("bankName", bankName);
                                                        totalSuccessfulCounter++;
                                                        try {
                                                            notificationService.sendEmailNotification(postMaturitySubject, notificationData, "email/post_maturity");
                                                        } catch (CustomCheckedException cce) {
                                                            cce.printStackTrace();
//                                    An error occurred while trying to send out notification, notify infotech of total failed and store failed mails in the db for retrial. Min of 3 retrials...
//                                                        failedCounter++;
                                                            if (!emailService.emailAlreadyFailed(obligatoryPaymentDate, toAddress, doRentalSubject)) {
                                                                failedCounter++;
                                                            }
                                                            log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
//                            failedCounter++;
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
//            this.notifyTeamOfOperation("Post Maturity", totalSuccessfulCounter, failedCounter);
            this.logDispatchOperation("Post Maturity", totalSuccessfulCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performChequeLodgementOperation() {
        try {
            Long totalSuccessfulCounter = 0L;
            Long failedCounter = 0L;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if (!clients.isEmpty()) {
                    for (Client client : clients) {
                        try {
                            CollectionOfficer collectionOfficer = collectionOfficerService.getCollectionOfficer(client.getBranchName());
                            Branch branch = branchService.getBranch(client.getBranchName());
                            if(!branch.getIsEnabled()) {
                                log.info("Branch {} is disabled from receiving notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                            }
                            NotificationConfig notificationConfig = notificationConfigService.getNotificationConfig(branch.getId(), NotificationType.CHEQUE_LODGEMENT.name());
                            if(notificationConfig != null) {
                                if(!notificationConfig.getIsEnabled()) {
                                    log.info("Branch {} is disabled from receiving cheque lodgement notifications. Hence, notification would not be sent out for client with ID {}", client.getBranchName(), client.getExternalID());
                                    throw new CustomCheckedException("Customer branch is disabled. Notification would not be sent out");
                                }
                            }
                            LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                            List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                    .stream()
//                                .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                    .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().contains("ARREARS"))
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
                                                    if (collectionOfficer == null) {
                                                        coN = defaultCollectionOfficer;
                                                        coE = collectionEmail;
                                                        coP = collectionPhoneNumber;
                                                    } else {
                                                        coN = collectionOfficer.getOfficerName();
                                                        coE = collectionOfficer.getOfficerEmail();
                                                        coP = collectionOfficer.getOfficerPhoneNo();
                                                    }
                                                    BigDecimal rentalAmount = thisMonthInstalment.getCurrentState().getPrincipalDueAmount().add(thisMonthInstalment.getCurrentState().getInterestDueAmount());
                                                    if(thisMonthInstalment.getCurrentState().getFeeDueAmount() != null)
                                                        rentalAmount = rentalAmount.add(thisMonthInstalment.getCurrentState().getFeeDueAmount());
                                                    if(rentalAmount.compareTo(BigDecimal.ZERO) > 0) {
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
                                                        totalSuccessfulCounter++;
                                                        try {
                                                            notificationService.sendEmailNotification(chequeLodgementSubject, notificationData, "email/cheque_lodgement");
                                                        } catch (CustomCheckedException cce) {
                                                            cce.printStackTrace();
//                                                        failedCounter++;
                                                            if (!emailService.emailAlreadyFailed(obligatoryPaymentDate, toAddress, doRentalSubject)) {
                                                                failedCounter++;
                                                            }
                                                            log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
                            failedCounter++;
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                } else {
                    lastExternalId = null;
                }
            }
//            this.notifyTeamOfOperation("Cheque Lodgement", totalSuccessfulCounter, failedCounter);
            this.logDispatchOperation("Cheque Lodgement", totalSuccessfulCounter, failedCounter);
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
                        try {
                            LookUpClient lookUpClient = clientService.lookupClient(cardDetails.getClientId());
                            String clientStatus = lookUpClient.getClient().getClientStatus();
                            if (clientStatus.equals("ACTIVE") || clientStatus.contains("ARREARS")) {
                                List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                        .stream()
//                                    .filter(cl -> !cl.getStatus().equalsIgnoreCase("CLOSED"))
                                        .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().contains("ARREARS"))
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
                                            List<LookUpLoanInstalment> loanInstalmentsLtOrEqToday = loanInstalments
                                                    .stream()
                                                    .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateLtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                                    .collect(Collectors.toList());
                                            if (!loanInstalmentsLtOrEqToday.isEmpty()) {
                                                for (LookUpLoanInstalment dueDateInstalment : loanInstalmentsLtOrEqToday) {
                                                    Client customer = lookUpClient.getClient();
                                                    LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(dueDateInstalment.getObligatoryPaymentDate());
                                                    String toAddress = customer.getEmail();
                                                    var principalDueAmount = dueDateInstalment.getCurrentState().getPrincipalDueAmount();
                                                    if (principalDueAmount.compareTo(BigDecimal.ZERO) > 0) {
                                                        BigDecimal totalDue = principalDueAmount
                                                                .add(dueDateInstalment.getCurrentState().getInterestDueAmount());
                                                        if(dueDateInstalment.getCurrentState().getFeeDueAmount() != null)
                                                            totalDue = totalDue.add(dueDateInstalment.getCurrentState().getFeeDueAmount());
                                                        BigDecimal newTotalDue = totalDue.multiply(new BigDecimal(100));
                                                        cardDetailsService.cardRecurringCharges(toAddress, newTotalDue, clientLoan.getId(), obligatoryPaymentDate, customer.getExternalID());
                                                    }
                                                }
//                                                List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalmentsLtOrEqToday
//                                                        .stream()
//                                                        .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
//                                                        .collect(Collectors.toList());
//                                                LookUpLoanInstalment dueDateInstalment = !lookUpLoanInstalments.isEmpty() ? lookUpLoanInstalments.get(0) : null;
////                                            System.out.println("Loan due date installment is: " + dueDateInstalment);
//                                                if (dueDateInstalment != null) {
////                                                System.out.println("Loan due date installment date is: " + dueDateInstalment.getObligatoryPaymentDate());
//                                                    Client customer = lookUpClient.getClient();
//                                                    LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(dueDateInstalment.getObligatoryPaymentDate());
//                                                    String toAddress = customer.getEmail();
//                                                    BigDecimal totalDue = dueDateInstalment.getCurrentState().getPrincipalDueAmount()
//                                                            .add(dueDateInstalment.getCurrentState().getInterestDueAmount());
//                                                    BigDecimal newTotalDue = totalDue.multiply(new BigDecimal(100));
////                                            String toAddress = useDefaultMailInfo ? defaultToAddress : customer.getEmail();
////                                            Perform recurring charge...
////                                                cardDetailsService.cardRecurringCharges(toAddress, totalDue, clientLoan.getId(), obligatoryPaymentDate, customer.getExternalID());
//                                                    cardDetailsService.cardRecurringCharges(toAddress, newTotalDue, clientLoan.getId(), obligatoryPaymentDate, customer.getExternalID());
//                                                }
                                            } else
                                                log.info("Loan installments gt or eq to today is empty...");
                                        } else log.info("Loan installments is empty...");
                                    } else log.info("Mode of repayment is not known..." + modeOfRepayment);
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
                            log.info("An error occurred for client with id: ".toUpperCase() + cardDetails.getClientId());
                        }
                    }
                    pageNumber++;
                } else {
                    pageNumber = null;
                }
            }
        }catch (Exception cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performRecurringMandateDebitInstruction() {
        try {
            Integer pageNumber = 0;
            log.info("GETTING THE PAGE NUMBER OF ZERO");
            while (pageNumber != null) {
                log.info("GETTING THE PAGE NUMBER {}",pageNumber);
                List<Mandates> mandates = remitaService.getAllActiveMandates(pageNumber, 100);
                log.info("ENTRY---> THE MANDATE FOR REMITA SERVICE {}",mandates);
                if (!mandates.isEmpty()) {
                    for (Mandates m : mandates) {
                        try {
                            log.info("STARTING THE MANDATE FOR REMITTA SERVICE PROCESSING {}",m.getClientId());
                            LookUpClient lookUpClient = clientService.lookupClient(m.getClientId());
                            String clientStatus = lookUpClient.getClient().getClientStatus();
                            if (clientStatus.equals("ACTIVE") || clientStatus.contains("ARREARS")) {
                                log.info("GETING THE CLIENT STATUS AS ACTIVE");
                                List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                        .stream()
                                        .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().contains("ARREARS"))
                                        .collect(Collectors.toList());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                                log.info("GETTING THE OPEN CLIENT LOAN LIST {}",openClientLoanList);
                                if (!openClientLoanList.isEmpty()) {
                                    log.info("GETTING THE CLINET LOAN LIST IS NOT EMPTY");
                                    LookUpClientLoan clientLoan = openClientLoanList.get(0);
                                    LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
                                    String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
                                            "" :
                                            lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
                                    if (modeOfRepayment.equalsIgnoreCase(remitaModeOfRepaymentKey)) {
                                        log.info("GETTING THE LOAN INSTALLMENT {}");
                                        List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                        if (!loanInstalments.isEmpty()) {
                                            List<LookUpLoanInstalment> loanInstalmentsLtOrEqToday = loanInstalments
                                                    .stream()
                                                    .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateLtOrEqToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                                                    .collect(Collectors.toList());
                                            log.info("GETTING  LOAN INSTALLMENT LATE TO TODAY {}",loanInstalmentsLtOrEqToday);
                                            if (!loanInstalmentsLtOrEqToday.isEmpty()) {
                                                for (LookUpLoanInstalment dueDateInstalment : loanInstalmentsLtOrEqToday) {
                                                    Client customer = lookUpClient.getClient();
                                                    LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(dueDateInstalment.getObligatoryPaymentDate());
                                                    var principalDueAmount = dueDateInstalment.getCurrentState().getPrincipalDueAmount();
                                                    if (principalDueAmount.compareTo(BigDecimal.ZERO) > 0) {
                                                        BigDecimal totalDue = principalDueAmount
                                                                .add(dueDateInstalment.getCurrentState().getInterestDueAmount());
                                                        if(dueDateInstalment.getCurrentState().getFeeDueAmount() != null)
                                                            totalDue = totalDue.add(dueDateInstalment.getCurrentState().getFeeDueAmount());
                                                        BigDecimal newTotalDue = totalDue.multiply(new BigDecimal(100));
                                                        cardDetailsService.initiateRemitaRecurringCharges(newTotalDue, clientLoan.getId(), obligatoryPaymentDate, customer.getExternalID());
                                                    }
                                                }
                                            } else
                                                log.info("Loan installments gt or eq to today is empty...");
                                        } else log.info("Loan installments is empty...");
                                    } else log.info("Mode of repayment is not known..." + modeOfRepayment);
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
                            log.info("An error occurred for client with id: ".toUpperCase() + m.getClientId());
                        }
                    }
                    pageNumber++;
                } else {
                    pageNumber = null;
                }
            }
        }catch (Exception cce) {
            cce.printStackTrace();
        }
    }

    @Override
    public void performMiscOperation(LocalDate startDate, LocalDate endDate) {
        try {
            Integer pageNumber = 0;
            int counter = 0;

            while (pageNumber != null) {
                List<CardDetails> tokenizedCardDetails = cardDetailsService.getAllCardDetails(pageNumber, 100);
                if (!tokenizedCardDetails.isEmpty()) {
                    for (CardDetails cardDetails : tokenizedCardDetails) {
                        log.info("CARD DETAILS: "+ cardDetails.getClientId());
                        try {
                            LookUpClient lookUpClient = clientService.lookupClient(cardDetails.getClientId());
                            String clientStatus = lookUpClient.getClient().getClientStatus();
                            log.info("LOOKUP DONE: "+ clientStatus);
                            if (clientStatus.equals("ACTIVE") || clientStatus.contains("ARREARS")) {
                                log.info("CLIENT IS ACTIVE/IN ARREARS");
                                List<LookUpClientLoan> openClientLoanList = lookUpClient.getLoans()
                                        .stream()
                                        .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().contains("ARREARS"))
                                        .collect(Collectors.toList());
                                log.info("OPEN CLIENT LOAN SIZE IS: "+ openClientLoanList.size());
//                Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                                if (!openClientLoanList.isEmpty()) {
                                    LookUpClientLoan clientLoan = openClientLoanList.get(0);
                                    LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(clientLoan.getId());
                                    log.info("LOAN ACCOUNT IS: "+ lookUpLoanAccount.getLoanAccount());
                                    String modeOfRepayment = lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment() == null ?
                                            "" :
                                            lookUpLoanAccount.getLoanAccount().getOptionalFields().getModeOfRepayment();
                                    log.info("MODE OF REPAYMENT IS: "+ modeOfRepayment);
                                    if (modeOfRepayment.equalsIgnoreCase(cardModeOfRepaymentKey)) {
                                        List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                                        log.info("INSTALMENT SIZE IS: "+ loanInstalments.size());
                                        if (!loanInstalments.isEmpty()) {
                                            List<LookUpLoanInstalment> loanInstalmentsWithin = loanInstalments
                                                    .stream()
                                                    .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateWithin(lookUpLoanInstalment.getObligatoryPaymentDate(), startDate, endDate))
                                                    .collect(Collectors.toList());
                                            log.info("INSTALMENT WITHIN SIZE IS: "+ loanInstalmentsWithin.size());
                                            if (!loanInstalmentsWithin.isEmpty()) {
                                                log.info("INSTALMENT WITHIN IS NOT EMPTY");
                                                for (LookUpLoanInstalment dueDateInstalment : loanInstalmentsWithin) {
                                                    log.info("DUE DATE INSTALMENT IS: "+ dueDateInstalment.getStatus());
                                                    Client customer = lookUpClient.getClient();
                                                    LocalDate obligatoryPaymentDate = dateUtil.convertDateToLocalDate(dueDateInstalment.getObligatoryPaymentDate());
                                                    String toAddress = customer.getEmail();
                                                    var principalDueAmount = dueDateInstalment.getCurrentState().getPrincipalDueAmount();
                                                    log.info("PRINCIPAL DUE AMOUNT IS: "+ principalDueAmount);
                                                    if (principalDueAmount.compareTo(BigDecimal.ZERO) > 0) {
                                                        log.info("PRINCIPAL DUE AMOUNT IS GT ZERO");
                                                        BigDecimal totalDue = principalDueAmount
                                                                .add(dueDateInstalment.getCurrentState().getInterestDueAmount());
                                                        if(dueDateInstalment.getCurrentState().getFeeDueAmount() != null)
                                                            totalDue = totalDue.add(dueDateInstalment.getCurrentState().getFeeDueAmount());
                                                        BigDecimal newTotalDue = totalDue.multiply(new BigDecimal(100));
                                                        cardDetailsService.cardRecurringCharges(toAddress, newTotalDue, clientLoan.getId(), obligatoryPaymentDate, customer.getExternalID());
                                                    }
                                                }
                                                log.info("Done with OP...." + cardDetails.getClientId());
                                            }else log.info("There are no loan instalments for client: "+ cardDetails.getClientId());
                                        } else log.info("Loan installments is empty...");
                                    } else log.info("Mode of repayment is not known..." + modeOfRepayment);
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
                            log.info("An error occurred for client with id: ".toUpperCase() + cardDetails.getClientId());
                        }
                        counter ++;
                    }
                    pageNumber++;
                } else {
                    pageNumber = null;
                }
            }
            log.info("TOTAL OPERATION COUNT AF IS: "+ counter);
        }catch (Exception cce) {
            cce.printStackTrace();
        }
    }

//    private void notifyTeamOfOperation(String operationName, int totalMailCounter, int failedCounter) throws CustomCheckedException {
//        if(totalMailCounter > 0) {
//            int totalSuccessfulCount = (totalMailCounter - failedCounter);
//            Map<String, String> notificationData = new HashMap<>();
//            notificationData.put("toAddress", defaultToAddress);
//            notificationData.put("toName", defaultToName);
//            notificationData.put("totalSuccessfulCount", String.valueOf(totalSuccessfulCount));
//            notificationData.put("totalFailedCount", String.valueOf(failedCounter));
//            notificationData.put("operationName", operationName);
//            notificationService.sendEmailNotification(dispatchedMailsSubject, notificationData, "email/dispatched_mails");
//        }
//    }

    @Override
    public void notifyTeamOfOperation() throws CustomCheckedException {
        List<MailMonitor> mailMonitorList = mailMonitorService.getAllDailyEventOperations();
        if(!mailMonitorList.isEmpty()) {
            for (MailMonitor monitoredEvent : mailMonitorList) {
                Map<String, String> notificationData = new HashMap<>();
                notificationData.put("toAddress", defaultToAddress);
                notificationData.put("toName", defaultToName);
                notificationData.put("totalSuccessfulCount", String.valueOf(monitoredEvent.getSuccessCount()));
                notificationData.put("totalFailedCount", String.valueOf(monitoredEvent.getFailedCount()));
                notificationData.put("operationName", monitoredEvent.getOperationName());
                notificationService.sendEmailNotification(dispatchedMailsSubject, notificationData, "email/dispatched_mails");
            }
        }
    }

    private void logDispatchOperation(String operationName, Long successCount, Long failureCount) {
        try {
            mailMonitorService.modifyDailyMonitor(operationName, successCount, failureCount);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
            log.info("AN ERROR OCCURRED WHILE TRYING TO LOG DISPATCH OPERATION. SEE ERROR: \n" + cce.getMessage());
        }
    }

    @Override
    public void sendOutEidNotification() {
        try {
            Long totalSuccessfulCounter = 0L;
            Long failedCounter = 0L;
            String lastExternalId = "";
            while (lastExternalId != null) {
                List<Client> clients = clientService.fetchClients(lastExternalId);
                if(!clients.isEmpty()) {
                    for(Client client : clients) {
                        try {
                            LookUpClient lookUpClient = clientService.lookupClient(client.getExternalID());
                            Client customer = lookUpClient.getClient();
                            String toAddress = customer.getEmail();
                            if (!emailService.alreadySentOutEmailToday(
                                    toAddress,
                                    customer.getName(),
                                    "Out of Office Notification",
                                    LocalDate.now()
                            )) {
                                Map<String, String> notificationData = new HashMap<>();
                                notificationData.put("toName", customer.getName());
                                notificationData.put("toAddress", toAddress);
                                notificationData.put("customerName", customer.getName());
                                totalSuccessfulCounter++;
                                try {
                                    notificationService.sendEmailNotification("Out of Office Notification", notificationData, "email/eid_holiday");
                                } catch (CustomCheckedException cce) {
                                    cce.printStackTrace();
                                    if(!emailService.emailAlreadyFailed(LocalDate.now(), toAddress, "Out of Office Notification")) {
                                        failedCounter++;
                                    }
                                    log.info("Failed to send out mail to: " + customer.getName() + ". See reason: " + cce.getMessage());
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    Client lastClient = clients.get((clients.size() - 1));
                    lastExternalId = lastClient.getExternalID();
                }else {
                    lastExternalId = null;
                }
            }
            this.logDispatchOperation("Eid-Holiday Notification", totalSuccessfulCounter, failedCounter);
        }catch (CustomCheckedException cce) {
            cce.printStackTrace();
        }
    }
}
