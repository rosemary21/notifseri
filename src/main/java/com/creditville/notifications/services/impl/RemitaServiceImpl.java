package com.creditville.notifications.services.impl;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.executor.HttpCallService;
import com.creditville.notifications.models.DTOs.CardTransactionsDto;
import com.creditville.notifications.models.DTOs.DebitInstructionDTO;
import com.creditville.notifications.models.Mandates;
import com.creditville.notifications.models.requests.*;
import com.creditville.notifications.models.response.*;
import com.creditville.notifications.repositories.MandateRepository;
import com.creditville.notifications.services.CardTransactionsService;
import com.creditville.notifications.services.RemitaService;
import com.creditville.notifications.utils.CardUtil;
import com.creditville.notifications.utils.DateUtil;
import com.creditville.notifications.utils.GeneralUtil;
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
import java.util.*;


@Slf4j
@Service
public class RemitaServiceImpl implements RemitaService {
    @Value("${remita.marchant.id}")
    private String marchantId;

    @Value("${remita.service.type.id}")
    private String serviceTypeId;

    @Value("${remita.mandate.type}")
    private String mandateType;

    @Value("${remita.frequency}")
    private String frequency;

    @Value("${remita.api.key}")
    private String apiKey;

    @Value(("${remita.api.token}"))
    private String apiToken;

    @Value("${remita.base.url}")
    private String baseUrl;

    @Value(("${remita.debit.url}"))
    private String debitUrl;

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
    private DateUtil dateUtil;

    @Autowired
    private CardTransactionsService cardTransactionsService;

    private String remitaActiveMandatesStatusCode = "00";

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
                System.out.println("I got here!!");
                //repay Loan
//                var loanRepaymentResp = repayLoan(debitDto);

                cardTransactionsService.saveCardTransaction(convertDebitObjectToCardTransactionDto(debitRespObj,debitDto));
            }
            return om.readValue(cardUtil.getObjectString(debitResp),MandateResp.class);
        } catch (JsonProcessingException | ParseException e) {
            e.printStackTrace();
            throw new CustomCheckedException("Unable to debit client, error reads: "+ e.getMessage());
        }
    }

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

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateRequestID(){
        var d = new Date();
        return String.valueOf(d.getTime());
    }

    private void saveMandate(Mandates mandates){
        mandateRepo.save(mandates);
    }

    private void updateMandate(Mandates mandates){
        var mandate = mandateRepo.findByMandateId(mandates.getMandateId());
        if(mandates.getRemitaTransRef() != null) {
            mandate.setRemitaTransRef(mandates.getRemitaTransRef());
        }
        if(null != mandates.getActivationStatus()){
            mandate.setActivationStatus(mandates.getActivationStatus());
            if(null != mandates.getRequestId()){
                mandate.setRequestId(mandates.getRequestId());
            }
        }
        saveMandate(mandate);
    }

    private HeaderParam getHeadersParam(String requestId, String hashParam){
        HeaderParam hp = new HeaderParam();
        hp.setMerchantId(marchantId);
        hp.setApiKey(apiKey);
        hp.setRequestId(requestId);
        hp.setRequest_ts(dateUtil.getTimeStamp());

        hp.setApiDetailsHash(hashParam);
        return hp;
    }

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
        if(mandates.getTotalElements() == 0) return new ArrayList<>();
        else return mandates.getContent();
    }
}
