package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.instafin.common.AppConstants;
import com.creditville.notifications.instafin.req.RepayLoanReq;
import com.creditville.notifications.instafin.service.LoanRepaymentService;
import com.creditville.notifications.models.*;
import com.creditville.notifications.models.DTOs.*;
import com.creditville.notifications.models.response.LookUpLoanAccount;
import com.creditville.notifications.models.response.MandateResp;
import com.creditville.notifications.repositories.CardDetailsRepository;
import com.creditville.notifications.repositories.CardTransactionRepository;
import com.creditville.notifications.repositories.MandateRepository;
import com.creditville.notifications.services.*;
import com.creditville.notifications.utils.CardUtil;
import com.creditville.notifications.utils.GeneralUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class CardDetailsServiceImpl implements CardDetailsService {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HttpCallService httpCallService;
    @Autowired
    private CardDetailsRepository cardDetailsRepo;
    @Autowired
    private CardUtil cardUtil;
    @Autowired
    private CardTransactionsService ctService;
    @Autowired
    private LoanRepaymentService loanRepaymentService;
    @Autowired
    RetryLoanRepaymentService retryLoanRepaymentService;
    @Autowired
    private ClientService clientService;

    @Value("${paystack.base.url}")
    private String psBaseUrl;
    @Value("${paystack.charge.auth.url}")
    private String psChargeAuthUrl;
    @Value("${paystack.trans.verification.url}")
    private String psTransVerification;

    @Value("${mail.cardTokenizationFailureSubject}")
    private String cardTokenizationFailureSubject;

    @Value("${mail.cardTokenizationSuccessSubject}")
    private String cardTokenizationSuccessSubject;

    @Value("${mail.repaymentFailureSubject}")
    private String repaymentFailureSubject;

    @Value("${mail.repaymentSuccessSubject}")
    private String repaymentSuccessSubject;

    @Value("${app.cardTokenizationUrl}")
    private String cardTokenizationUrl;

    @Value("${paystack.partial.debit.url}")
    private String partialDebitUrl;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.paystackToName}")
    private String tokenizationName;

    @Value("${app.paystack.tokenization.email}")
    private String tokenizationEmail;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PartialDebitService partialDebitService;

    @Autowired
    private MandateRepository mandateRepository;

    @Autowired
    private RemitaService remitaService;

    @Autowired
    private GeneralUtil gu;

    @Override
    public void saveCustomerCardDetails(CardDetails cardDetails) {
        cardDetailsRepo.save(cardDetails);
    }

    @Override
    public void cardAuthorization(String reference, String clientId) {
        CardDetailsDto cdDto = new CardDetailsDto();
        var authResp = verifyTransaction(reference, gu.isClientTG(clientId));
        cdDto.setPaystackResponse(authResp);
        boolean isTokenized = false;
        try {
            var tranRespObj = cardUtil.getJsonObjResponse(authResp);
            log.info("tranRespObj: "+tranRespObj);
            if(null != tranRespObj && tranRespObj.containsKey("data")){
                var dataObj =(JSONObject) tranRespObj.get("data");
                var customerObj = (JSONObject) dataObj.get("customer");
                var authObj = (JSONObject) dataObj.get("authorization");

                cdDto.setAmount(new BigDecimal(dataObj.get("amount").toString()));
                cdDto.setStatus(dataObj.get("status").toString());
                cdDto.setReference(dataObj.get("reference").toString());
                cdDto.setChannel(dataObj.get("channel").toString());

                cdDto.setFirstName(cardUtil.checkNullStr(customerObj.get("first_name").toString()));
                cdDto.setLastName(cardUtil.checkNullStr(customerObj.get("last_name").toString()));
                cdDto.setEmail(customerObj.get("email").toString());

                cdDto.setAuthorizationCode(authObj.get("authorization_code").toString());
                cdDto.setSignature(authObj.get("signature").toString());

                cdDto.setClientId(clientId);

                saveCustomerCardDetails(convertCardDetailsDtoToEntity(cdDto));
                log.info("ENTRY cardAuthorization: >>>>>>>>> SAVED <<<<<<<<<<");
                isTokenized = true;
            }
            String clientEmail = cdDto.getEmail();
            String clientName = cdDto.getFirstName() + " " + cdDto.getLastName();
            Map<String, String> notificationData = new HashMap<>();
            notificationData.put("toName", tokenizationName);
            notificationData.put("toAddress", tokenizationEmail);
            notificationData.put("clientEmail", clientEmail);
            notificationData.put("clientId", cdDto.getClientId());
            notificationData.put("clientName", clientName);
            try {
                String subject = isTokenized ? cardTokenizationSuccessSubject : cardTokenizationFailureSubject;
                String templateLocation = isTokenized ? "email/card-successfully-tokenized" : "email/card-not-tokenized";
                notificationService.sendEmailNotification(subject, notificationData, templateLocation);
            }catch (CustomCheckedException cce) {
                cce.printStackTrace();
                log.info("An error occurred while trying to notify team of tokenization status");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void cardRecurringCharges(String email, BigDecimal amount, String loanId, LocalDate currentDate, String clientID) {
//        ChargeDto chargeDto = new ChargeDto();
//        CardTransactionsDto ctDTO = new CardTransactionsDto();
//        RepayLoanReq repayLoanReq = new RepayLoanReq();
//        boolean repaymentStatus = true;
//
//        var cardDetails = cardDetailsRepo.findByClientIdAndEmail(clientID, email);
//        String errorMessage = null;
//        if(null == cardDetails){
//            System.out.println("Card is not tokenized".toUpperCase());
//            repaymentStatus = false;
//        }else {
//            try {
//                CardTransactions existingTransaction = cardTransactionRepository.findByCardDetailsAndStatusInAndLastUpdate(cardDetails, Collections.singletonList("success"), currentDate);
//                if (existingTransaction == null) {
//                    chargeDto.setAmount(amount);
//                    chargeDto.setAuthorization_code(cardDetails.getAuthorizationCode());
//                    chargeDto.setEmail(email);
//
//                    var chargeResp = chargeCard(chargeDto);
//
//                    ctDTO.setPaystackResponse(chargeResp);
//
//                    var chargeRespObj = cardUtil.getJsonObjResponse(chargeResp);
//                    System.out.println("ENTRY -> recurringCharges response: " + chargeRespObj);
//                    if (null != chargeRespObj && chargeRespObj.containsKey("data")) {
//                        var dataObj = (JSONObject) chargeRespObj.get("data");
//                        var authObj = (JSONObject) dataObj.get("authorization");
//
//                        ctDTO.setAmount(new BigDecimal(dataObj.get("amount").toString()));
//                        ctDTO.setCurrency(dataObj.get("currency").toString());
//                        ctDTO.setTransactionDate(dataObj.get("transaction_date").toString());
//                        ctDTO.setStatus(dataObj.get("status").toString());
//                        ctDTO.setReference(dataObj.get("reference").toString());
//
//                        ctDTO.setCardType(authObj.get("card_type").toString());
//
//                        ctDTO.setCardDetails(cardDetails);
//
//                        ctService.saveCardTransaction(ctDTO);
//
//                        //repay Loan
//                        repayLoanReq.setAccountID(loanId);
//                        repayLoanReq.setAmount(amount);
//                        repayLoanReq.setPaymentMethodName("Cash");
//                        repayLoanReq.setTransactionBranchID("CVLHQB");
//                        repayLoanReq.setRepaymentDate(currentDate.toString());
//                        repayLoanReq.setNotes("Card loan repayment");
//                        var repaymentResp = loanRepaymentService.makeLoanRepayment(repayLoanReq);
//                        if (null == repaymentResp) {
//                            repaymentStatus = false;
//                            errorMessage = chargeRespObj.get("message").toString();
//                        }else {
//                            if(repaymentResp.trim().equals("")) {
//                                repaymentStatus = false;
//                                errorMessage = chargeRespObj.get("message").toString();
//                            }
//                        }
//                    }else repaymentStatus = false;
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//        Map<String, String> notificationData = new HashMap<>();
//        notificationData.put("toName", tokenizationName);
////        notificationData.put("toAddress", tokenizationEmail);
//        notificationData.put("toAddress", email);
//        notificationData.put("loanId", loanId);
//        notificationData.put("todayDate", LocalDate.now().toString());
//        notificationData.put("failureMessage", errorMessage);
//        notificationData.put("paymentDate", currentDate.toString());
//        String mailSubject = repaymentStatus ? repaymentSuccessSubject : repaymentFailureSubject;
//        String templateLocation = repaymentStatus ? "email/repayment-success" : "email/repayment-failure";
//        if(!emailService.alreadySentOutEmailToday(email, tokenizationName, mailSubject, currentDate)) {
//            try {
//                notificationService.sendEmailNotification(mailSubject, notificationData, templateLocation);
//            } catch (CustomCheckedException cce) {
//                cce.printStackTrace();
//                System.out.println("An error occurred while trying to notify team of repayment status");
//            }
//        }
//    }

    @Override
    public void cardRecurringCharges(String email, BigDecimal amount, String loanId, LocalDate currentDate, String clientID,String obligDate, BigDecimal transactionFee) {
        log.info("Email {}, Amount {}, Loan ID {}, Local Date {}, ClientID: {}", email, amount.toString(), loanId, currentDate.toString(), clientID);
        ChargeDto chargeDto = new ChargeDto();
        CardTransactionsDto ctDTO = new CardTransactionsDto();
        RepayLoanReq repayLoanReq = new RepayLoanReq();
        boolean repaymentStatus = true;

        var cardDetails = cardDetailsRepo.findByClientIdAndEmail(clientID, email);
        String errorMessage = null;
        if(null == cardDetails){
            log.info("Card is not tokenized".toUpperCase());
            repaymentStatus = false;
        }else {
            try {
                CardTransactions existingTransaction = cardTransactionRepository.findByCardDetailsAndStatusInAndLastUpdate(cardDetails, Collections.singletonList("success"), currentDate);
                if (existingTransaction == null) {
                    log.info("There is no such existing transaction. Creating one now...");
                    if(amount.compareTo(BigDecimal.ZERO) > 0) {
                        chargeDto.setAmount(amount);
                        chargeDto.setAuthorization_code(cardDetails.getAuthorizationCode());
                        chargeDto.setEmail(email);
                        var chargeResp = chargeCard(chargeDto, gu.isClientTG(clientID));
                        ctDTO.setPaystackResponse(chargeResp);

                        var chargeRespObj = cardUtil.getJsonObjResponse(chargeResp);
//                        log.info("ENTRY -> recurringCharges response: " + chargeRespObj);
                        if (null != chargeRespObj && chargeRespObj.containsKey("data")) {
                            log.info("Data response was gotten from PAYSTACK for client: {} with loan id: {}", cardDetails.getClientId(), loanId);
                            var dataObj = (JSONObject) chargeRespObj.get("data");
                            var authObj = (JSONObject) dataObj.get("authorization");

                            BigDecimal chargedAmount = new BigDecimal(dataObj.get("amount").toString());
                            BigDecimal newChargedAmount = chargedAmount.divide(new BigDecimal(100)).setScale(2, RoundingMode.CEILING);

                            BigDecimal loanAmt = newChargedAmount.subtract(transactionFee).setScale(2, RoundingMode.CEILING);

                            ctDTO.setAmount(loanAmt);
                            ctDTO.setTotalDebitAmount(newChargedAmount);
                            ctDTO.setCurrency(dataObj.get("currency").toString());
                            ctDTO.setTransactionDate(dataObj.get("transaction_date").toString());
                            ctDTO.setStatus(dataObj.get("status").toString());
                            ctDTO.setReference(dataObj.get("reference").toString());

                            ctDTO.setCardType(authObj.get("card_type").toString());
                            ctDTO.setPaystackFee(transactionFee);

                            ctDTO.setCardDetails(cardDetails);

                            var savedCardTransaction = ctService.saveCardTransaction(ctDTO);
                            if(ctDTO.getStatus().equalsIgnoreCase("success")){
                                //repay Loan
                                repayLoanReq.setAccountID(loanId);
//                                repayLoanReq.setAmount(new BigDecimal(dataObj.get("amount").toString()));
                                var loanAmount = newChargedAmount.subtract(transactionFee);
                                repayLoanReq.setAmount(loanAmount);
                                String paymentMethod;
                                String transactionBranchID;
                                if(!gu.isClientTG(clientID)){
                                    paymentMethod = AppConstants.InstafinPaymentMethod.PAYSTACK_PAYMENT_METHOD;
                                    transactionBranchID = AppConstants.InstafinBranch.TRANSACTION_BRANCH_ID;
                                }else {
                                    paymentMethod = AppConstants.TG_InstafinPaymentMethod.TG_PAYSTACK_PAYMENT_METHOD;
                                    transactionBranchID = AppConstants.InstafinBranch.CMFB_TRANSACTION_BRANCH_ID;
                                }
                                repayLoanReq.setPaymentMethodName(paymentMethod);
                                repayLoanReq.setTransactionBranchID(transactionBranchID);
                                repayLoanReq.setRepaymentDate(currentDate.toString());
                                repayLoanReq.setNotes("Card loan repayment"+" Loan ID : "+loanId+" Reference Id : "+ctDTO.getReference());
                                var repaymentResp = loanRepaymentService.makeLoanRepayment(repayLoanReq);
                                if(repaymentResp != null) {
                                    JSONObject repaymentResponseObject;
                                    try{
                                        repaymentResponseObject = cardUtil.getJsonObjResponse(repaymentResp);
                                        if(responseContainsValidationError(repaymentResponseObject)) {
                                            errorMessage = repaymentResponseObject.get("message").toString();
                                            repaymentStatus = false;
                                        }
                                    }catch (Exception ex) {
                                        ex.printStackTrace();
                                        repaymentResponseObject = null;
                                    }
                                    if(repaymentResponseObject == null) {
                                        boolean isEmpty = repaymentResp.trim().equals("");
                                        errorMessage = isEmpty ?
                                                "Charge successful but loan repayment failed. Reason: No response gotten from Instafin" :
                                                repaymentResp;
                                        if(isEmpty) repaymentStatus = false;
                                    }
                                }else {
                                    errorMessage = "Charge successful but loan repayment failed. Reason: No response gotten from Instafin";
                                    repaymentStatus = false;
                                }
                                if(!repaymentStatus) {
//                                  ctDTO.setStatus("repayment_failure");
//                                  ctDTO.setInstafinResponse(errorMessage);
//                                  ctService.saveCardTransaction(ctDTO);
                                    savedCardTransaction.setStatus("repayment_failure");
                                    savedCardTransaction.setInstafinResponse(errorMessage);
                                    ctService.addCardTransaction(savedCardTransaction);
                                    RetryLoanRepaymentDTO retryLoanRepaymentDTO=retryLoanRepaymentService.getLoanRepayment(savedCardTransaction,loanId,email,obligDate);
                                    retryLoanRepaymentDTO.setMethodOfRepayment("paystack");
                                    retryLoanRepaymentDTO.setInstafinRepayAmt(loanAmount);
                                    retryLoanRepaymentService.saveRetryLoan(savedCardTransaction,retryLoanRepaymentDTO,loanId);

                                }else {
//                                    ctDTO.setInstafinResponse("REPAYMENT SUCCESSFUL");
//                                    ctService.saveCardTransaction(ctDTO);
                                    savedCardTransaction.setStatus("REPAYMENT SUCCESSFUL");
                                    ctService.addCardTransaction(savedCardTransaction);
                                }
                            }else {
//                                Charge failed. Attempt PD...
                                Map<String, String> pdResponse = this.performPd(dataObj, chargeDto, repayLoanReq, loanId, currentDate, clientID);
                                repaymentStatus = Boolean.valueOf(pdResponse.get("repaymentStatus"));
                                errorMessage = pdResponse.get("errorMessage");
                            }

                        }else {
                            log.info("No response was gotten from PAYSTACK. Aborting operation for client: {} with loan id: {}", cardDetails.getClientId(), loanId);
                            repaymentStatus = false;
                            errorMessage = "No response gotten from paystack";
                        }
                    }else {
//                        Customer is no longer owing...
                        PartialDebit partialDebit = partialDebitService.getPartialDebit(
                                chargeDto.getAuthorization_code(),
                                chargeDto.getAmount(),
                                chargeDto.getEmail()
                        );
                        if(partialDebit != null){
                            partialDebitService.deletePartialDebitRecord(partialDebit.getId());
                        }
                    }
                }else {
                    log.info("An existing transaction already exists. See ID: "+ existingTransaction.getId());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //business do not require notification of failed transaction 13//05/2022

//        Map<String, String> notificationData = new HashMap<>();
//        notificationData.put("toName", tokenizationName);
//        notificationData.put("customerName", tokenizationName);
//        notificationData.put("toAddress", tokenizationEmail);
////        notificationData.put("toAddress", email);
//        notificationData.put("loanId", loanId);
//        notificationData.put("todayDate", LocalDate.now().toString());
//        notificationData.put("failureMessage", errorMessage);
//        notificationData.put("paymentDate", currentDate.toString());
//        String mailSubject = repaymentStatus ? repaymentSuccessSubject : repaymentFailureSubject;
//        String templateLocation = repaymentStatus ? "email/repayment-success" : "email/repayment-failure";
//        if(!emailService.alreadySentOutEmailToday(email, tokenizationName, mailSubject, currentDate)) {
//            try {
//                notificationService.sendEmailNotification(mailSubject, notificationData, templateLocation);
//            } catch (CustomCheckedException cce) {
//                cce.printStackTrace();
//                log.info("An error occurred while trying to notify team of repayment status: Error message: " +  cce.getMessage());
//            }
//        }
        // business do not require notification of failed transaction 13//05/2022
        
    }

    @Override
    public void initiateRemitaRecurringCharges(BigDecimal amount, String loanId, LocalDate currentDate, String clientID) {
        log.info("Amount {}, Loan ID {}, Local Date {}, ClientID: {}", amount.toString(), loanId, currentDate.toString(), clientID);
        Mandates mandate = mandateRepository.findByClientIdAndLoanId(clientID, loanId);
        if(null == mandate){
            log.info("Mandate record does not exist...".toUpperCase());
        }else {
            CardTransactions existingTransaction = cardTransactionRepository.findByRemitaRequestIdAndMandateIdAndStatusInAndTransactionDate(mandate.getRequestId(), mandate.getMandateId(), Arrays.asList("success", "pending"), currentDate.toString());
            if (existingTransaction == null) {
                log.info("There is no such existing transaction. Creating one now...");
                if(amount.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("Getting the amount greater than zero");
                    DebitInstructionDTO debitInstructionDTO = new DebitInstructionDTO();
                    debitInstructionDTO.setTotalAmount(String.valueOf(amount));
                    debitInstructionDTO.setFundingAccount(mandate.getAccount());
                    debitInstructionDTO.setFundingBankCode(mandate.getBankCode());
                    debitInstructionDTO.setClientId(mandate.getClientId());
                    debitInstructionDTO.setLoanId(mandate.getLoanId());
                    MandateResp debitInstructionResp = null;
                    try {
                        debitInstructionResp = remitaService.sendDebitInstruction(debitInstructionDTO);
                    } catch (CustomCheckedException e) {
                        e.printStackTrace();
                        return;
                    }
                    if(debitInstructionResp == null) return;
                    CardTransactionsDto ctDTO = new CardTransactionsDto();
                    ctDTO.setRemitaResponse(debitInstructionResp.toString());
                    ctDTO.setAmount(mandate.getAmount());
                    ctDTO.setCurrency("NGN");
                    ctDTO.setTransactionDate(currentDate.toString());
                    ctDTO.setStatus("pending");
                    ctDTO.setReference(debitInstructionResp.getTransactionRef());
                    ctDTO.setMandateId(debitInstructionResp.getMandateId());
//                    ctDTO.setRemitaRequestId(mandate.getRequestId());
                    ctDTO.setRemitaRequestId(debitInstructionResp.getRequestId());
                    ctService.saveCardTransaction(ctDTO);
                }else {
//                        Customer is no longer owing...
                    System.out.println("Customer is no longer owing".toUpperCase());
                }
            }else {
                log.info("An existing transaction already exists. See ID: "+ existingTransaction.getId());
            }
        }
    }

    private Map<String, String> performPd(JSONObject chargeRespObj, ChargeDto chargeDto, RepayLoanReq repayLoanReq, String loanId, LocalDate currentDate, String clientId) throws ParseException {
        String errorMessage;
        Map<String, String> responseMap = new HashMap<>();
        CardTransactionsDto ctDTO = new CardTransactionsDto();
        var cardDetails = cardDetailsRepo.findByClientIdAndEmail(clientId, chargeDto.getEmail());
        if (chargeRespObj.get("gateway_response") != null && (chargeRespObj.get("gateway_response").toString().equalsIgnoreCase("insufficient funds") || chargeRespObj.get("gateway_response").toString().equalsIgnoreCase("not sufficient funds"))) {
//          Charge failed due to insufficient funds. Attempt partial debit...
            PartialDebit partialDebit = partialDebitService.getPartialDebit(
                    chargeDto.getAuthorization_code(),
                    chargeDto.getAmount(),
                    chargeDto.getEmail());
            boolean isNewPdRecord = false;
            boolean maxAttemptsReached = false;
            if (partialDebit == null) isNewPdRecord = true;
            else {
//              Check that partial debit attempts has not exceeded maximum (4)
                if(partialDebitService.getPartialDebitAttempt(partialDebit, LocalDate.now()).getTotalNoOfAttempts() == 4)
                    maxAttemptsReached = true;
            }
            if(!maxAttemptsReached) {
                String pdResp = this.makePartialDebit(new PartialDebitDto(
                        chargeDto.getAuthorization_code(),
                        chargeDto.getAmount(),
                        chargeDto.getEmail()), gu.isClientTG(clientId));
                ctDTO.setPaystackResponse(pdResp);
                if (pdResp != null) {
                    JSONObject pdRespObj = cardUtil.getJsonObjResponse(pdResp);
                    if (pdRespObj != null && pdRespObj.containsKey("data")) {
                        JSONObject data = cardUtil.getJsonObjResponse(pdRespObj.get("data").toString());
                        if (data.get("status").toString().equalsIgnoreCase("success")) {
//                          Partial debit successful...
                            BigDecimal pdAmount = new BigDecimal(data.get("amount").toString());
                            BigDecimal newPdAmount = pdAmount.divide(new BigDecimal(100)).setScale(2, RoundingMode.CEILING);

                            ctDTO.setAmount(newPdAmount);
                            ctDTO.setCurrency(pdRespObj.get("currency").toString());
                            ctDTO.setTransactionDate(pdRespObj.get("transaction_date").toString());
                            ctDTO.setStatus(pdRespObj.get("status").toString());
                            ctDTO.setReference(pdRespObj.get("reference").toString());

                            ctDTO.setCardType(pdRespObj.get("card_type").toString());

                            ctDTO.setCardDetails(cardDetails);

                            var savedCardTransaction = ctService.saveCardTransaction(ctDTO);

//                          Make loan repayment...
                            String paymentMethod;
                            String transactionBranch;
                            if(!gu.isClientTG(clientId)){
                                paymentMethod = AppConstants.InstafinPaymentMethod.PAYSTACK_PAYMENT_METHOD;
                                transactionBranch = AppConstants.InstafinBranch.TRANSACTION_BRANCH_ID;
                            }else {
                                paymentMethod = AppConstants.TG_InstafinPaymentMethod.TG_PAYSTACK_PAYMENT_METHOD;
                                transactionBranch = AppConstants.InstafinBranch.CMFB_TRANSACTION_BRANCH_ID;
                            }
                            repayLoanReq.setPaymentMethodName(paymentMethod);
                            repayLoanReq.setAccountID(loanId);
                            repayLoanReq.setAmount(newPdAmount);
                            repayLoanReq.setTransactionBranchID(transactionBranch);
                            repayLoanReq.setRepaymentDate(currentDate.toString());
                            repayLoanReq.setNotes("Paystack Card loan repayment "+" Loan Id: "+loanId+" Reference Id: "+ctDTO.getReference());
                            var repaymentResp = loanRepaymentService.makeLoanRepayment(repayLoanReq);
                            if(repaymentResp != null) {
                                JSONObject repaymentResponseObject;
                                try{
                                    repaymentResponseObject = cardUtil.getJsonObjResponse(repaymentResp);
                                    if(responseContainsValidationError(repaymentResponseObject)) {
                                        errorMessage = repaymentResponseObject.get("message").toString();
                                        responseMap.put("repaymentStatus", Boolean.toString(false));
                                    }else {
                                        errorMessage = null;
                                        responseMap.put("repaymentStatus", Boolean.toString(true));
//                                        Repayment successful...
                                        if (isNewPdRecord)
                                            partialDebitService.savePartialDebit(chargeDto.getAuthorization_code(), loanId, chargeDto.getAmount(), chargeDto.getEmail(), currentDate);
                                        else {
                                            PartialDebitAttempt partialDebitAttempt = partialDebitService.getPartialDebitAttempt(partialDebit, LocalDate.now());
                                            int totalAttempts = partialDebitAttempt.getTotalNoOfAttempts();
                                            int totalAttemptsInc = (totalAttempts + 1);
                                            partialDebitAttempt.setTotalNoOfAttempts(totalAttemptsInc);
                                            partialDebitService.savePartialDebitAttempt(partialDebitAttempt);
                                        }
                                    }
                                    responseMap.put("errorMessage", errorMessage);
                                }catch (Exception ex) {
                                    ex.printStackTrace();
                                    repaymentResponseObject = null;
                                }
                                if(repaymentResponseObject == null) {
                                    boolean isEmpty = repaymentResp.trim().equals("");
                                    errorMessage = isEmpty ?
                                            "Charge successful but loan repayment failed. Reason: No response gotten from Instafin" :
                                            repaymentResp;
                                    if(isEmpty) {
                                        responseMap.put("repaymentStatus", Boolean.toString(false));
                                        responseMap.put("errorMessage", errorMessage);
                                    }
                                }
                            }else {
                                errorMessage = "Charge successful but loan repayment failed. Reason: No response gotten from Instafin";
                                responseMap.put("repaymentStatus", Boolean.toString(false));
                                responseMap.put("errorMessage", errorMessage);
                            }

                            if(responseMap.get("repaymentStatus").equals("false")) {
                                savedCardTransaction.setStatus("repayment_failure");
                                savedCardTransaction.setInstafinResponse(responseMap.get("errorMessage"));
                                ctService.addCardTransaction(savedCardTransaction);
                            }else {
                                savedCardTransaction.setStatus("REPAYMENT SUCCESSFUL");
                                ctService.addCardTransaction(savedCardTransaction);
                            }
                        }
                    }
                }
            }
        }else {
            if(chargeRespObj.get("gateway_response") != null) {
                responseMap.put("repaymentStatus", Boolean.toString(false));
                responseMap.put("errorMessage", chargeRespObj.get("gateway_response").toString());
            }else {
                responseMap.put("repaymentStatus", Boolean.toString(false));
                responseMap.put("errorMessage", "No gateway response returned from Paystack");
            }
        }
        return responseMap;
    }

    @Override
    public String makePartialDebit(PartialDebitDto partialDebitDto, boolean isClientTG) {
        String resp = null;
        try {
            var payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(partialDebitDto);
            log.info("ENTRY -> makePartialDebit payload: "+ payload);
            resp = httpCallService.httpPaystackCall(psBaseUrl+partialDebitUrl, payload, isClientTG);
            log.info("ENTRY -> makePartialDebit resp: "+resp);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return resp;
    }


    @Override
    public String chargeCard(ChargeDto chargeDto, boolean isTGClient) {
        String chargeResp = null;
        try {
            var payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(chargeDto);
            log.info("ENTRY -> recurringCharges payload: "+ payload);
            chargeResp = httpCallService.httpPaystackCall(psBaseUrl+psChargeAuthUrl, payload, isTGClient);
            log.info("recurringCharges response: "+chargeResp);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return chargeResp;
    }

    @Override
    public String verifyTransaction(String reference, boolean isClientTG) {
        log.info("Verify transaction reference: "+reference);
        var transResp = httpCallService.httpPaystackCall(psBaseUrl+psTransVerification+reference, null, isClientTG);
//        System.out.println("transResp: "+transResp);
        return transResp;
    }

    private CardDetails convertCardDetailsDtoToEntity(CardDetailsDto cdDto){
        return cardUtil.modelMapper().map(cdDto, CardDetails.class);
    }

    private CardDetailsDto convertCardDetailsEntityTODTO(CardDetails cardDetails){
        return cardUtil.modelMapper().map(cardDetails,CardDetailsDto.class);
    }

    @Override
    public List<CardDetails> getAllCardDetails(Integer pageNo, Integer pageSize) {
        return cardDetailsRepo.findAllByStatusIn(Collections.singletonList("success"), PageRequest.of(pageNo, pageSize)).getContent();
    }

    private boolean responseContainsValidationError(JSONObject jsonObject){
        String[] validationErrors = new String[] {"LEGACY_VALIDATION_ERROR", "VALIDATION",
                "NON_EXISTING_ACCOUNT", "INVALID_STATUS_CHANGE",
                "ACCOUNT_ALREADY_DISBURSED", "NON_EXISTING_ACCOUNT_STATUS", "VALUE_BEFORE_APPROVAL_DATE",
                "CLIENTS_NOT_FOUND", "PAYMENT_METHOD_UNAVAILABLE", "NON_EXISTING_BRANCH", "STATUS_CHANGE_DATE_INVALID",
                "DISBURSEMENT_NOT_ALLOWED", "GENERIC_VALIDATION_ERROR"};
        boolean contains = false;
        for(String error : validationErrors) {
            if(jsonObject.containsValue(error)) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
