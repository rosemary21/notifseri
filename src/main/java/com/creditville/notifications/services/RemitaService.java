package com.creditville.notifications.services;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.DTOs.DebitInstructionDTO;
import com.creditville.notifications.models.Mandates;
import com.creditville.notifications.models.requests.RemitaDebitStatus;
import com.creditville.notifications.models.response.MandateResp;

import java.util.List;

public interface RemitaService {
    MandateResp sendDebitInstruction(DebitInstructionDTO debitInstructionReq) throws CustomCheckedException;

    List<Mandates> getAllActiveMandates(Integer pageNumber, Integer pageSize);

    MandateResp checkDebitStatusAndRepayLoan(RemitaDebitStatus rds) throws CustomCheckedException;


}
