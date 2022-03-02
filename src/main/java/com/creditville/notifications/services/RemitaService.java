package com.creditville.notifications.services;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.DTOs.DebitInstructionDTO;
import com.creditville.notifications.models.Mandates;
import com.creditville.notifications.models.requests.MandateReq;
import com.creditville.notifications.models.requests.RemitaDebitStatus;
import com.creditville.notifications.models.response.MandateResp;
import com.creditville.notifications.models.response.RemitaDebitStatusResp;

import java.util.List;

public interface RemitaService {
    MandateResp sendDebitInstruction(DebitInstructionDTO debitInstructionReq) throws CustomCheckedException;

    List<Mandates> getAllActiveMandates(Integer pageNumber, Integer pageSize);

    void checkDebitStatusAndRepayLoan() ;

     String generateRemitaDebitStatusHash(String... params);

     String generateRemitaHMAC512Hash(String... params);

    RemitaDebitStatusResp checkRemitaTransactionStatus(RemitaDebitStatus rds);

    List<Mandates> getAllNoneActiveMandates(Integer pageNo, Integer pageSize);

    MandateResp getMandateActivationStatus(MandateReq mReq);
}
