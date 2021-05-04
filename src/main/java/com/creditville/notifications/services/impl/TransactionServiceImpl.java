package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.instafin.common.AppConstants;
import com.creditville.notifications.instafin.req.RepayLoanReq;
import com.creditville.notifications.instafin.service.LoanRepaymentService;
import com.creditville.notifications.models.CardDetails;
import com.creditville.notifications.models.DTOs.CardTransactionsDto;
import com.creditville.notifications.models.DTOs.PartialDebitDto;
import com.creditville.notifications.models.PartialDebit;
import com.creditville.notifications.models.PartialDebitAttempt;
import com.creditville.notifications.models.requests.HookEvent;
import com.creditville.notifications.models.requests.HookEventAuthorization;
import com.creditville.notifications.models.requests.HookEventData;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.repositories.CardDetailsRepository;
import com.creditville.notifications.services.*;
import com.creditville.notifications.utils.CardUtil;
import com.creditville.notifications.utils.DateUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Value("${paystack.charge.message}")
    private String successfulChargeEvent;

    @Autowired
    private CardDetailsRepository cardDetailsRepository;

    @Autowired
    private CardTransactionsService cardTransactionsService;

    @Autowired
    private LoanRepaymentService loanRepaymentService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Value("${mail.repaymentFailureSubject}")
    private String repaymentFailureSubject;

    @Value("${mail.repaymentSuccessSubject}")
    private String repaymentSuccessSubject;

    @Value("${app.paystackToName}")
    private String tokenizationName;

    @Value("${app.paystack.tokenization.email}")
    private String tokenizationEmail;

    @Autowired
    private PartialDebitService partialDebitService;

    @Autowired
    private CardDetailsService cardDetailsService;

    @Autowired
    private CardUtil cardUtil;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public void handlePaystackTransactionEvent(HookEvent hookEvent) {
        if(hookEvent.getEvent().equalsIgnoreCase(successfulChargeEvent)) {
//            Charge was successful...
//            Check if customer has an auth code...
            if(hookEvent.getData() != null) {
                if(hookEvent.getData().getAuthorization() != null) {
                    CardDetails customerRecord = cardDetailsRepository.findByAuthorizationCode(hookEvent.getData().getAuthorization().getAuthorizationCode());
                    if(customerRecord == null) {
//                        Customer'a auth code is not found. Hence, this is a tokenization process. Do nothing because tokenization requires a response to front end...
                        System.out.println("Received hook event for tokenization but no operation was performed due to the structure of our system...");
                    }else {
//                        Customer's record is found. Hence, this is a repayment process. Attempt repayment...
                        CardTransactionsDto ctDTO = new CardTransactionsDto();
                        RepayLoanReq repayLoanReq = new RepayLoanReq();
                        LocalDate currentDate = LocalDate.now();
                        boolean repaymentStatus = true;
                        String errorMessage = null;
                        String loanId = null;
                        var dataObj = hookEvent.getData();
                        var authObj = hookEvent.getData().getAuthorization();

                        BigDecimal chargedAmount = dataObj.getAmount();
                        BigDecimal newChargedAmount = chargedAmount.divide(new BigDecimal(100)).setScale(2, RoundingMode.CEILING);

//                            ctDTO.setAmount(new BigDecimal(dataObj.get("amount").toString()));
                        ctDTO.setAmount(newChargedAmount);
                        ctDTO.setCurrency(dataObj.getCurrency());
                        ctDTO.setTransactionDate(dataObj.getPaidAt());
                        ctDTO.setStatus(dataObj.getStatus());
                        ctDTO.setReference(dataObj.getReference());

                        ctDTO.setCardType(authObj.getCardType());

                        ctDTO.setCardDetails(customerRecord);

                        cardTransactionsService.saveCardTransaction(ctDTO);
                        if(ctDTO.getStatus().equalsIgnoreCase("success")){
                            //repay Loan
                            try {
                                LookUpClient client = clientService.lookupClient(customerRecord.getClientId());

                                List<LookUpClientLoan> openClientLoanList = client.getLoans()
                                        .stream()
                                        .filter(cl -> cl.getStatus().equalsIgnoreCase("ACTIVE") || cl.getStatus().equalsIgnoreCase("IN_ARREARS"))
                                        .collect(Collectors.toList());
//                                    Since there can be only one open client loan at a time, check if the list is empty, if not, get the first element...
                                if (!openClientLoanList.isEmpty()) {
                                    LookUpClientLoan clientLoan = openClientLoanList.get(0);
//                                    Check if it's a pd operation before checking below. It is a pd operation if previous loan before this month is unsettled and ammount is or there is an unsettled bal...
//                                    If it is a pd operation, return unsettled loan ID...
                                    try {
                                        String unsettledLoanId = this.getUnsettledLoanId(clientLoan);
                                        loanId = unsettledLoanId != null ? unsettledLoanId : clientLoan.getId();
                                    }catch (CustomCheckedException cce) {
                                        System.out.println("Unable to get unsettled loan ID. See reason below: \n"+ cce.getMessage());
                                        loanId = clientLoan.getId();
                                    }
                                }
                            }catch (CustomCheckedException cce) {
                                System.out.println("An error occurred while performing lookup. See status below...");
                                cce.printStackTrace();
                            }
                            repayLoanReq.setAccountID(loanId);
//                                repayLoanReq.setAmount(new BigDecimal(dataObj.get("amount").toString()));
                            repayLoanReq.setAmount(newChargedAmount);
                            repayLoanReq.setPaymentMethodName(AppConstants.InstafinPaymentMethod.PAYSTACK_PAYMENT_METHOD);
                            repayLoanReq.setTransactionBranchID(AppConstants.InstafinBranch.TRANSACTION_BRANCH_ID);
                            repayLoanReq.setRepaymentDate(currentDate.toString());
                            repayLoanReq.setNotes("Card loan repayment");
                            var repaymentResp = loanRepaymentService.makeLoanRepayment(repayLoanReq);
                            if (null == repaymentResp) {
                                repaymentStatus = false;
                                errorMessage = dataObj.getMessage();
                            } else {
                                if (repaymentResp.trim().equals("")) {
                                    repaymentStatus = false;
                                    errorMessage = dataObj.getMessage();
                                }
                            }
                        }else {
                            repaymentStatus = false;
                            errorMessage = dataObj.getMessage();
                        }
//                        Send out operation notification...
                        this.sendOutOpNotification(loanId, errorMessage, currentDate, repaymentStatus);
                    }
                }else System.out.println("No Auuthorization information was returned for customer with email: "+ hookEvent.getData().getCustomer().getEmail());
            }
        }else {
            System.out.println("Charge was not successful. See status below: \n" + hookEvent.getEvent());
            String message = hookEvent.getData().getMessage();
            System.out.println("Unsuccessful charge message is: "+ message);
            if(message.equalsIgnoreCase("insufficient funds")) {
//                Charge failed due to insufficient funds. Attempt partial debit...
//                TODO check if PD is disabled for customer...
                if(hookEvent.getData() != null) {
                    if (hookEvent.getData().getAuthorization() != null) {
                        HookEventData data = hookEvent.getData();
                        HookEventAuthorization authorization = data.getAuthorization();
                        cardDetailsService.makePartialDebit(
                                new PartialDebitDto(
                                        authorization.getAuthorizationCode(),
                                        data.getAmount(),
                                        data.getCustomer().getEmail()
                                )
                        );
                    }
                }
            }
        }
    }

    private String getUnsettledLoanId(LookUpClientLoan activeLoan) throws CustomCheckedException {
        LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(activeLoan.getId());
        List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
        String unsettledLoanId = null;
        if (!loanInstalments.isEmpty()) {
            List<LookUpLoanInstalment> loanInstalmentsLtToday = loanInstalments
                    .stream()
                    .filter(lookUpLoanInstalment -> dateUtil.isPaymentDateLtToday(lookUpLoanInstalment.getObligatoryPaymentDate()))
                    .collect(Collectors.toList());
            if (!loanInstalmentsLtToday.isEmpty()) {
                LookUpLoanInstalment latestInstalment = loanInstalmentsLtToday.get((loanInstalmentsLtToday.size() - 1));
                if (latestInstalment.getCurrentState().getPrincipalDueAmount().compareTo(BigDecimal.ZERO) > 0) {
//                    Customer has loan in arrears
//                    I want to make payment only for this instalment, I feel ID would be the loan ID. If not, we are posed with the challenge of paying the right loan
                    unsettledLoanId = latestInstalment.getId();
                }
            }
        }
        return unsettledLoanId;
    }

    private void sendOutOpNotification(String loanId, String errorMessage, LocalDate currentDate, boolean repaymentStatus) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put("toName", tokenizationName);
        notificationData.put("customerName", tokenizationName);
        notificationData.put("toAddress", tokenizationEmail);
        notificationData.put("loanId", loanId);
        notificationData.put("todayDate", LocalDate.now().toString());
        notificationData.put("failureMessage", errorMessage);
        notificationData.put("paymentDate", currentDate.toString());
        String mailSubject = repaymentStatus ? repaymentSuccessSubject : repaymentFailureSubject;
        String templateLocation = repaymentStatus ? "email/repayment-success" : "email/repayment-failure";
        if(!emailService.alreadySentOutEmailToday(tokenizationEmail, tokenizationName, mailSubject, currentDate)) {
            try {
                notificationService.sendEmailNotification(mailSubject, notificationData, templateLocation);
            } catch (CustomCheckedException cce) {
                cce.printStackTrace();
                System.out.println("An error occurred while trying to notify team of repayment status");
            }
        }
    }

    private void performPdOperation(String authCode, BigDecimal amount, String email, RepayLoanReq repayLoanReq, String loanId, LocalDate currentDate) throws ParseException {
//        Partial debit operation...
        PartialDebit partialDebit = partialDebitService.getPartialDebit(authCode, amount, email);
        boolean isNewPdRecord = false;
        boolean maxAttemptsReached = false;
        boolean repaymentStatus = false;
        String errorMessage = null;
        if (partialDebit == null) isNewPdRecord = true;
        else {
//            Check that partial debit attempts has not exceeded maximum (4)
            if(partialDebitService.getPartialDebitAttempt(partialDebit, LocalDate.now()).getTotalNoOfAttempts() == 4)
                maxAttemptsReached = true;
        }
        if(!maxAttemptsReached) {
            String pdResp = cardDetailsService.makePartialDebit(new PartialDebitDto(authCode, amount, email));
            if (pdResp != null) {
                JSONObject pdRespObj = cardUtil.getJsonObjResponse(pdResp);
                if (pdRespObj != null) {
                    JSONObject data = cardUtil.getJsonObjResponse(pdRespObj.get("data").toString());
                    if (data.get("status").toString().equalsIgnoreCase("success")) {
//                        Partial debit successful...
                        BigDecimal pdAmount = new BigDecimal(data.get("amount").toString());
                        BigDecimal newPdAmount = pdAmount.divide(new BigDecimal(100)).setScale(2, RoundingMode.CEILING);
//                        Make loan repayment...
                        repayLoanReq.setAccountID(loanId);
//                        repayLoanReq.setAmount(pdAmount);
                        repayLoanReq.setAmount(newPdAmount);
                        repayLoanReq.setPaymentMethodName(AppConstants.InstafinPaymentMethod.PAYSTACK_PAYMENT_METHOD);
                        repayLoanReq.setTransactionBranchID(AppConstants.InstafinBranch.TRANSACTION_BRANCH_ID);
                        repayLoanReq.setRepaymentDate(currentDate.toString());
                        repayLoanReq.setNotes("Paystack Card loan repayment");
                        var repaymentResp = loanRepaymentService.makeLoanRepayment(repayLoanReq);
                        if (null == repaymentResp) {
                            repaymentStatus = false;
                            errorMessage = pdRespObj.get("message").toString();
                        } else {
                            if (repaymentResp.trim().equals("")) {
                                repaymentStatus = false;
                                errorMessage = pdRespObj.get("message").toString();
                            } else {
//                                Repayment successful...
                                if (isNewPdRecord)
                                    partialDebitService.savePartialDebit(authCode, loanId, amount, email, currentDate);
                                else {
                                    PartialDebitAttempt partialDebitAttempt = partialDebitService.getPartialDebitAttempt(partialDebit, LocalDate.now());
                                    int totalAttempts = partialDebitAttempt.getTotalNoOfAttempts();
                                    int totalAttemptsInc = (totalAttempts + 1);
                                    partialDebitAttempt.setTotalNoOfAttempts(totalAttemptsInc);
                                    partialDebitService.savePartialDebitAttempt(partialDebitAttempt);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
