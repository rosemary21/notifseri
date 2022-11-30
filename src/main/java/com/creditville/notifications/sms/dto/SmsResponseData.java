package com.creditville.notifications.sms.dto;

import com.creditville.notifications.sms.dto.vtpass.VtpassResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmsResponseData {


    private ResponseDTO respDTO;

    private  MultipleResponseDTO multipleRespDTO;

    private MultipleTicketResponseDTO multipleTicketRespDTO;

    private VtpassResponseDto vtpassResponseDto;
}
