package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.instafin.req.RepayLoanReq;
import com.creditville.notifications.instafin.service.LoanRepaymentService;
import com.creditville.notifications.models.DTOs.CardTransactionsDto;
import com.creditville.notifications.models.DTOs.ChargeDto;
import com.creditville.notifications.models.DTOs.PartialDebitDto;
import com.creditville.notifications.models.PartialDebit;
import com.creditville.notifications.models.PartialDebitAttempt;
import com.creditville.notifications.models.response.Client;
import com.creditville.notifications.models.response.LookUpLoanAccount;
import com.creditville.notifications.models.response.LookUpLoanInstalment;
import com.creditville.notifications.repositories.CardDetailsRepository;
import com.creditville.notifications.repositories.PartialDebitAttemptRepository;
import com.creditville.notifications.repositories.PartialDebitRepository;
import com.creditville.notifications.services.*;
import com.creditville.notifications.utils.CardUtil;
import com.creditville.notifications.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PartialDebitServiceImpl implements PartialDebitService {
    @Autowired
    private PartialDebitRepository partialDebitRepository;

    @Autowired
    private PartialDebitAttemptRepository partialDebitAttemptRepository;

    @Autowired
    private CardDetailsService cardDetailsService;

    @Autowired
    private CardUtil cardUtil;

    @Autowired
    private LoanRepaymentService loanRepaymentService;

    @Autowired
    private ClientService clientService;

    @Value("${app.card.modeOfRepaymentKey}")
    private String cardModeOfRepaymentKey;

    @Autowired
    private DateUtil dateUtil;

    @Value("${app.defaultToName}")
    private String tokenizationName;

    @Value("${app.defaultToAddress}")
    private String tokenizationEmail;

    @Value("${mail.repaymentFailureSubject}")
    private String repaymentFailureSubject;

    @Value("${mail.repaymentSuccessSubject}")
    private String repaymentSuccessSubject;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CardDetailsRepository cardDetailsRepo;

    @Autowired
    private CardTransactionsService ctService;

    @Override
    public PartialDebit savePartialDebit(String authCode, String loanId, BigDecimal amount, String email, LocalDate paymentDate) {
        PartialDebit partialDebit = partialDebitRepository.save(new PartialDebit(authCode, loanId, amount, email, paymentDate));
        partialDebitAttemptRepository.save(new PartialDebitAttempt(partialDebit));
        return partialDebit;
    }

    @Override
    public PartialDebit getPartialDebit(String authCode, BigDecimal amount, String email) {
        return partialDebitRepository.findByAuthorizationCodeAndAmountAndEmail(authCode, amount, email);
    }

    @Override
    public PartialDebitAttempt getPartialDebitAttempt(PartialDebit partialDebit, LocalDate date) {
        return partialDebitAttemptRepository.findByPartialDebitAndDate(partialDebit, date);
    }

    @Override
    public PartialDebitAttempt savePartialDebitAttempt(PartialDebitAttempt partialDebitAttempt) {
        return partialDebitAttemptRepository.save(partialDebitAttempt);
    }

    @Override
    public void deletePartialDebitRecord(Long partialDebitId) {
        Optional<PartialDebit> partialDebit = partialDebitRepository.findById(partialDebitId);
        if(partialDebit.isPresent()) {
            List<PartialDebitAttempt> partialDebitAttempts = partialDebitAttemptRepository.findAllByPartialDebit(partialDebit.get());
            partialDebitAttempts.forEach(pdAttempt -> partialDebitAttemptRepository.deleteById(pdAttempt.getId()));
            partialDebitRepository.deleteById(partialDebit.get().getId());
        }
    }

    @Override
    public List<PartialDebit> getAllPartialDebitRecords() {
        return partialDebitRepository.findAll();
    }

    @Override
    public void performPartialDebitOp() {
        for(PartialDebit pdRecord : this.getAllPartialDebitRecords()) {
            String errorMessage = null;
            boolean repaymentStatus = true;
            try {
                boolean maxAttemptsReached = false;
                boolean createNewPdAttempt = false;
                if(this.getPartialDebitAttempt(pdRecord, LocalDate.now()) != null) {
                    if (this.getPartialDebitAttempt(pdRecord, LocalDate.now()).getTotalNoOfAttempts() == 4)
                        maxAttemptsReached = true;
                }else createNewPdAttempt = true;

                LookUpLoanAccount lookUpLoanAccount = clientService.lookupLoanAccount(pdRecord.getLoanId());
                List<LookUpLoanInstalment> loanInstalments = lookUpLoanAccount.getLoanAccount().getInstalments();
                List<LookUpLoanInstalment> lookUpLoanInstalments = loanInstalments
                        .stream()
                        .filter(lookUpLoanInstalment -> dateUtil.paymentDateMatches(lookUpLoanInstalment.getObligatoryPaymentDate(), pdRecord.getPaymentDate()))
                        .collect(Collectors.toList());
                LookUpLoanInstalment loanInstalment = lookUpLoanInstalments.get(0);
                BigDecimal totalDue = loanInstalment.getCurrentState().getPrincipalDueAmount()
                        .add(loanInstalment.getCurrentState().getInterestDueAmount());
                BigDecimal newTotalDue = totalDue.multiply(new BigDecimal(100));
                if(totalDue.compareTo(BigDecimal.ZERO) > 0) {
//                    if(dateUtil.isPaymentDateBeforeOrWithinNumber(pdRecord.getPaymentDate(), 5)) {
////                        Partial debit operation not greater five(5) days yet...
//                        ChargeDto chargeDto = new ChargeDto();
                        RepayLoanReq repayLoanReq = new RepayLoanReq();
                        if (!maxAttemptsReached) {
                            CardTransactionsDto ctDTO = new CardTransactionsDto();
                            var cardDetails = cardDetailsRepo.findByClientIdAndEmail(lookUpLoanAccount.getClient().getExternalID(), pdRecord.getEmail());
                            String pdResp = cardDetailsService.makePartialDebit(new PartialDebitDto(
                                    pdRecord.getAuthorizationCode(),
                                    newTotalDue,
                                    pdRecord.getEmail()));
                            if (pdResp != null) {
                                JSONObject pdRespObj = cardUtil.getJsonObjResponse(pdResp);
                                if (pdRespObj != null) {
                                    JSONObject data = cardUtil.getJsonObjResponse(pdRespObj.get("data").toString());
                                    if (data.get("status").toString().equalsIgnoreCase("success")) {
//                                            Partial debit successful...
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

//                                            Make loan repayment...
                                        repayLoanReq.setAccountID(pdRecord.getLoanId());
                                        repayLoanReq.setAmount(newPdAmount);
                                        repayLoanReq.setPaymentMethodName("Cash");
                                        repayLoanReq.setTransactionBranchID("CVLHQB");
                                        repayLoanReq.setRepaymentDate(LocalDate.now().toString());
                                        repayLoanReq.setNotes("Paystack Card loan repayment");
                                        var repaymentResp = loanRepaymentService.makeLoanRepayment(repayLoanReq);
                                        if (null == repaymentResp) {
                                            repaymentStatus = false;
//                                            errorMessage = pdRespObj.get("message").toString();
                                        } else {
                                            if (repaymentResp.trim().equals("")) {
                                                repaymentStatus = false;
//                                                errorMessage = pdRespObj.get("message").toString();
                                            } else {
//                                                    Repayment successful...
                                                PartialDebitAttempt partialDebitAttempt = !createNewPdAttempt ? this.getPartialDebitAttempt(pdRecord, LocalDate.now()) : new PartialDebitAttempt(pdRecord);
                                                int totalAttempts = partialDebitAttempt.getTotalNoOfAttempts();
                                                int totalAttemptsInc = (totalAttempts + 1);
                                                partialDebitAttempt.setTotalNoOfAttempts(totalAttemptsInc);
                                                this.savePartialDebitAttempt(partialDebitAttempt);
                                            }
                                        }

                                        if(!repaymentStatus) {
                                            savedCardTransaction.setStatus("repayment_failure");
                                            boolean isEmpty = repaymentResp != null && repaymentResp.trim().equals("");
                                            errorMessage = isEmpty ?
                                                    "Charge successful but loan repayment failed. Reason: No response gotten from Instafin" :
                                                    repaymentResp;
                                            savedCardTransaction.setInstafinResponse(repaymentResp);
                                            ctService.addCardTransaction(savedCardTransaction);
                                        }else {
                                            savedCardTransaction.setStatus("REPAYMENT SUCCESSFUL");
                                            ctService.addCardTransaction(savedCardTransaction);
                                        }
                                    }
                                }
                            }
                        }
//                    }else {
////                        Partial debit date has passed 5th Day, delete record from pd table and contact risk team or collection officer...
//                        this.deletePartialDebitRecord(pdRecord.getId());
//                        errorMessage = "Unable to totally charge customer after five days or persistent trials";
//                    }
                }else {
//                        Customer is no longer owing...
                    this.deletePartialDebitRecord(pdRecord.getId());
                }
            }catch (Exception ex) {
                ex.printStackTrace();
                log.info("Partial debit operation failed for record with ID: "+ pdRecord.getId() + ". Reason \n "+ ex.getMessage());
            }
//            Notify team...
            Map<String, String> notificationData = new HashMap<>();
            notificationData.put("toName", tokenizationName);
            notificationData.put("customerName", tokenizationName);
//        notificationData.put("toAddress", tokenizationEmail);
            notificationData.put("toAddress", pdRecord.getEmail());
            notificationData.put("loanId", pdRecord.getLoanId());
            notificationData.put("todayDate", LocalDate.now().toString());
            notificationData.put("failureMessage", errorMessage);
            notificationData.put("paymentDate", LocalDate.now().toString());
            String mailSubject = repaymentStatus ? repaymentSuccessSubject : repaymentFailureSubject;
            String templateLocation = repaymentStatus ? "email/repayment-success" : "email/repayment-failure";
            if(!emailService.alreadySentOutEmailToday(pdRecord.getEmail(), tokenizationName, mailSubject, LocalDate.now())) {
                try {
                    notificationService.sendEmailNotification(mailSubject, notificationData, templateLocation);
                } catch (CustomCheckedException cce) {
                    cce.printStackTrace();
                    log.info("An error occurred while trying to notify team of repayment status. See message: "+ cce.getMessage());
                }
            }
        }
    }

    @Override
    public BigDecimal getLeastPartialDebitAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        BigDecimal thirtyPercentOfAmount = new BigDecimal("0.30");
        return amount.multiply(thirtyPercentOfAmount).setScale(2, RoundingMode.CEILING);
    }
}
