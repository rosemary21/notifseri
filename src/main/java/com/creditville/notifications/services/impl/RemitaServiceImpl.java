package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.instafin.common.AppConstants;
import com.creditville.notifications.instafin.req.RepayLoanReq;
import com.creditville.notifications.instafin.service.LoanRepaymentService;
import com.creditville.notifications.models.CardTransactions;
import com.creditville.notifications.models.DTOs.CardTransactionsDto;
import com.creditville.notifications.models.DTOs.DebitInstructionDTO;
import com.creditville.notifications.models.DTOs.RetryLoanRepaymentDTO;
import com.creditville.notifications.models.Mandates;
import com.creditville.notifications.models.RetryLoanRepayment;
import com.creditville.notifications.models.requests.*;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.repositories.CardTransactionRepository;
import com.creditville.notifications.repositories.MandateRepository;
import com.creditville.notifications.repositories.RetryLoanRepaymentRepository;
import com.creditville.notifications.services.CardTransactionsService;
import com.creditville.notifications.services.RemitaService;
import com.creditville.notifications.services.RetryLoanRepaymentService;
import com.creditville.notifications.utils.CardUtil;
//import com.creditville.notifications.utils.DateUtil;
import com.creditville.notifications.utils.DateUtil;
import com.creditville.notifications.utils.GeneralUtil;
import com.creditville.notifications.utils.ValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;


@Slf4j
@Service
public class RemitaServiceImpl implements RemitaService {
    @Value("${remita.marchant.id}")
    private String marchantId;

    @Value("${remita.service.type.id}")
    private String serviceTypeId;

//    @Value("${remita.mandate.type}")
//    private String mandateType;
//
//    @Value("${remita.frequency}")
//    private String frequency;

    @Value("${remita.api.key}")
    private String apiKey;

//    @Value("${remita.api.token}")
//    private String apiToken;

    @Value("${remita.base.url}")
    private String baseUrl;

    @Autowired
    RetryLoanRepaymentService retryLoanRepaymentService;

    @Value("${remita.debit.url}")
    private String debitUrl;
    @Value("${remita.debit.status.url}")
    private String debitStatusUrl;

    @Value("remita.mandate.status")
    private String mandateStatus;
    @Value("remita.none.active.mandate.statuscode")
    private String initiatedMandateStatuscode;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private HttpCallService httpCallService;

    @Autowired
    private MandateRepository mandateRepo;

    @Autowired
    private GeneralUtil generalUtil;

    @Autowired
    private CardUtil cardUtil;


    @Autowired
    private CardTransactionsService cardTransactionsService;
    @Autowired
    private CardTransactionRepository ctRepo;

    @Autowired
    private LoanRepaymentService lrs;
    @Autowired
    private ValidationUtil vu;
    @Autowired
    private CardTransactionsService ctService;
    @Autowired
    private RetryLoanRepaymentRepository rlrRepo;
    @Autowired
    private DateUtil du;

    private final String remitaActiveMandatesStatusCode = "00";

    @Override
    public MandateResp sendDebitInstruction(DebitInstructionDTO debitDto) throws CustomCheckedException {

        var clientDetails = mandateRepo.findByClientIdAndLoanId(debitDto.getClientId(),debitDto.getLoanId());
        var requestID = generateRequestID();
        System.out.println("requestID: "+requestID);

        var hash = generateRemitaHMAC512Hash(marchantId,serviceTypeId,requestID,debitDto.getTotalAmount(),apiKey);

        debitDto.setMerchantId(marchantId);
        debitDto.setServiceTypeId(serviceTypeId);
        debitDto.setHash(hash);
        debitDto.setRequestId(requestID);
        debitDto.setMandateId(clientDetails.getMandateId());

        try {
            var payload = om.writerWithDefaultPrettyPrinter().writeValueAsString(convertDebitInstructionDtoToReq(debitDto));
            System.out.println("ENTRY sendDebitInstruction -> payload: "+payload);
            var debitResp = httpCallService.remitaHttpUrlCall(baseUrl + debitUrl,payload,null);
            System.out.println("ENTRY sendDebitInstruction -> debitResp: "+debitResp);
            var debitRespObj = cardUtil.getJsonObjResponse(cardUtil.getObjectString(debitResp));

            if(debitRespObj.get("statuscode").toString().equalsIgnoreCase("069")){
                cardTransactionsService.saveCardTransaction(convertDebitObjectToCardTransactionDto(debitRespObj,debitDto));
            }
            return om.readValue(cardUtil.getObjectString(debitResp),MandateResp.class);
        } catch (JsonProcessingException | ParseException e) {
            e.printStackTrace();
            throw new CustomCheckedException("Unable to debit client, error reads: "+ e.getMessage());
        }
    }

    @Override
    public String generateRemitaHMAC512Hash(String... params) {

        StringBuilder sb = new StringBuilder();
        for(String param : params) sb.append(param);

        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(sb.toString().getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            System.out.println("hashtext outside: "+hashtext);
            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateRemitaDebitStatusHash(String... params) {
        String generatedPassword = null;
//        String salt = getSalt();
        StringBuilder sbs = new StringBuilder();
        for(String param : params) sbs.append(param);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
//            md.update(salt.getBytes());
            byte[] bytes = md.digest(sbs.toString().getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    // Add salt
    private static String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt.toString();
    }

    private String generateRequestID(){
        var d = new Date();
        return String.valueOf(d.getTime());
    }

//    private void saveMandate(Mandates mandates){
//        mandateRepo.save(mandates);
//    }

//    private void updateMandate(Mandates mandates){
//        var mandate = mandateRepo.findByMandateId(mandates.getMandateId());
//        if(mandates.getRemitaTransRef() != null) {
//            mandate.setRemitaTransRef(mandates.getRemitaTransRef());
//        }
//        if(null != mandates.getActivationStatus()){
//            mandate.setActivationStatus(mandates.getActivationStatus());
//            if(null != mandates.getRequestId()){
//                mandate.setRequestId(mandates.getRequestId());
//            }
//        }
//        saveMandate(mandate);
//    }

//    private HeaderParam getHeadersParam(String requestId, String hashParam){
//        HeaderParam hp = new HeaderParam();
//        hp.setMerchantId(marchantId);
//        hp.setApiKey(apiKey);
//        hp.setRequestId(requestId);
//        hp.setRequest_ts(dateUtil.getTimeStamp());
//
//        hp.setApiDetailsHash(hashParam);
//        return hp;
//    }

    private DebitInstructionReq convertDebitInstructionDtoToReq(DebitInstructionDTO debitInstructionDTO){
        return generalUtil.modelMapper().map(debitInstructionDTO,DebitInstructionReq.class);
    }

    private CardTransactionsDto convertDebitObjectToCardTransactionDto(JSONObject o , DebitInstructionDTO dReq){
        CardTransactionsDto ctDto = new CardTransactionsDto();
        ctDto.setAmount(new BigDecimal(dReq.getTotalAmount()));
        ctDto.setCardType("REMITA");
        ctDto.setReference(o.get("transactionRef").toString());
        ctDto.setStatus(o.get("status").toString());
        ctDto.setRemitaResponse(o.toJSONString());
        ctDto.setRrr(o.get("RRR").toString());
        ctDto.setAmount(new BigDecimal(dReq.getTotalAmount()));
        ctDto.setRemitaRequestId(o.get("requestId").toString());

        return ctDto;
    }

    @Override
    public List<Mandates> getAllActiveMandates(Integer pageNumber, Integer pageSize) {
        Page<Mandates> mandates = mandateRepo.findAllByStatusCode(remitaActiveMandatesStatusCode, PageRequest.of(pageNumber, pageSize));
        log.info("ENTRY-> GETTING THE SIZE OF MANDATES {}",mandates);
        if(mandates.getTotalElements() == 0) return new ArrayList<>();
        else return mandates.getContent();
    }

    @Override
    public void checkDebitStatusAndRepayLoan()  {

        Collection<CardTransactions> ctList =  ctRepo.findAllByStatusAndMandateIdIsNotNullAndRemitaRequestIdIsNotNull("pending");
        log.info("getting the c list {}",ctList.size());
        if(null != ctList){
            ctList.forEach(ct -> {
                RemitaDebitStatus rds = new RemitaDebitStatus();

                rds.setRequestId(ct.getRemitaRequestId());
                rds.setMandateId(ct.getMandateId());
//                var hash = generateRemitaHMAC512Hash(ct.getMandateId(),marchantId,ct.getRemitaRequestId(), apiKey);
                var hash = generateRemitaDebitStatusHash(ct.getMandateId(),marchantId,ct.getRemitaRequestId(), apiKey);
                rds.setHash(hash);
                rds.setMerchantId(marchantId);
                var statusResp = checkRemitaTransactionStatus(rds);
                log.info("getting the status response {}",statusResp);

                if(null != statusResp){
                    Mandates mandate = mandateRepo.findByMandateId(statusResp.getMandateId());
                    RepayLoanReq rlr = new RepayLoanReq();
                    rlr.setAccountID(mandate.getLoanId());
                    rlr.setAmount(statusResp.getAmount());
                    rlr.setPaymentMethodName(AppConstants.InstafinPaymentMethod.REMITA_PAYMENT_METHOD);
                    rlr.setTransactionBranchID(AppConstants.InstafinBranch.TRANSACTION_BRANCH_ID);
                    rlr.setRepaymentDate(ct.getLastUpdate().toString());
                    rlr.setNotes("Remita loan repayment - loan ID: - "+mandate.getLoanId() +
                            " mandate ID: - "+mandate.getMandateId() + "transactionRef: - " +statusResp.getTransactionRef());
//                                        rlr.setNotes("Remita loan repayment - loan ID: - "+mandate.getLoanId() +
//                            " mandate ID: - "+mandate.getMandateId() );
                    var repaymentResp = lrs.makeLoanRepayment(rlr);
                    log.info("getting the repayment response {}",repaymentResp);
                    String errorMgs = null;
                    boolean repaymentStatus = true;
                    if(null != repaymentResp){
                        JSONObject repaymentRespObj;
                          try {
                            repaymentRespObj = cardUtil.getJsonObjResponse(repaymentResp);
                            if(repaymentRespObj == null){
                                errorMgs = repaymentResp;
                                repaymentStatus = false;

                            } else if(vu.responseContainsValidationError(repaymentRespObj)){
                                errorMgs = repaymentRespObj.get("message").toString();
                                repaymentStatus = false;
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                            repaymentStatus = false;
                        }
                    }else {
                        errorMgs = "No response gotten from Instafin";
                        repaymentStatus = false;
                    }

//                    var ctDetails = ctRepo.findByRemitaRequestIdAndMandateId(ct.getRemitaRequestId(),ct.getMandateId());
                    if(!repaymentStatus){
                        ct.setStatus("repayment_failure");
                        ct.setInstafinResponse(errorMgs);
                        RetryLoanRepayment lr = new RetryLoanRepayment();
                        lr.setLoanId(mandate.getLoanId());
                        lr.setNoOfRetry(0);
                        lr.setStatus("repayment_failure");
                        lr.setClientId(mandate.getClientId());
                        lr.setProcessFlag("N");
                        lr.setStatus("Pending");
                        CardTransactions cardTransactions= ctService.addCardTransaction(ct);
                        if(cardTransactions.getCardDetails() == null){
                            RetryLoanRepaymentDTO retryLoanRepaymentDTO=retryLoanRepaymentService.getLoanMandateRepayment(cardTransactions,mandate.getLoanId(),"260606348065",ct.getTransactionDate());
                            retryLoanRepaymentDTO.setMethodOfRepayment("Remitta");
                            retryLoanRepaymentService.saveRetryLoan(cardTransactions,retryLoanRepaymentDTO,mandate.getLoanId());
                        }
                    }else {
                        ct.setStatus("REPAYMENT SUCCESSFUL");
                        CardTransactions cardTransactions= ctService.addCardTransaction(ct);

                    }
                }
            });
        }
    }

    @Override
    public RemitaDebitStatusResp checkRemitaTransactionStatus(RemitaDebitStatus rds){
        try {
            var payload = om.writerWithDefaultPrettyPrinter().writeValueAsString(rds);
            log.info("ENTRY checkRemitaTransactionStatus -> payload: {} ",payload);
            var statusResp = httpCallService.remitaHttpUrlCall(baseUrl + debitStatusUrl,payload,null);
            log.info("ENTRY checkRemitaTransactionStatus -> statusResp: {} ",statusResp);
            var statusRespObj = cardUtil.getJsonObjResponse(cardUtil.getObjectString(statusResp));
            if((statusRespObj.get("statuscode").toString().equalsIgnoreCase("072")) ||((statusRespObj.get("statuscode").toString().equalsIgnoreCase("071"))||((statusRespObj.get("statuscode").toString().equalsIgnoreCase("00"))) )){
                return om.readValue(cardUtil.getObjectString(statusResp), RemitaDebitStatusResp.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MandateResp getMandateActivationStatus(MandateReq mReq){
        var header = getHeadersParam(mReq.getRequestId(),mReq.getHash());

        try {
            var payload = om.writerWithDefaultPrettyPrinter().writeValueAsString(mReq);
            log.info("ENTRY checkMandateActivationStatus -> payload: {} ",payload);
            var resp =  httpCallService.remitaHttpUrlCall(baseUrl + mandateStatus, payload, header);
            log.info("ENTRY checkMandateActivationStatus -> resp: {} ",resp);
            var respObj = cardUtil.getJsonObjResponse(cardUtil.getObjectString(resp));
            if(null != respObj)return om.readValue(cardUtil.getObjectString(resp), MandateResp.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HeaderParam getHeadersParam (String requestId, String hashParam){
        HeaderParam hp = new HeaderParam();
        hp.setMerchantId(marchantId);
        hp.setApiKey(apiKey);
        hp.setRequestId(requestId);
        hp.setRequest_ts(du.getTimeStamp());

        hp.setApiDetailsHash(hashParam);
        return hp;
    }

    @Override
    public List<Mandates> getAllNoneActiveMandates(Integer pageNo, Integer pageSize){
        Page<Mandates> mPage = mandateRepo.findAllByStatusCode(initiatedMandateStatuscode, PageRequest.of(pageNo, pageSize));
        log.info("ENTRY getAllNonActiveMandates -> mPage: {} ",mPage);
        if(mPage.getTotalElements() == 0) return new ArrayList<>();
        else return mPage.getContent();
    }
}
