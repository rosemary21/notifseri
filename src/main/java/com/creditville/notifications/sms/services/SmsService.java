package com.creditville.notifications.sms.services;

import com.creditville.notifications.sms.dto.*;

public interface SmsService {
    ResponseDTO sendSingleSms(SMSDTO requestDTO);
    MultipleResponseDTO sendMultipleSms(MultipleSMSDTO multipleSMSDTO);
    MultipleResponseDTO sendBulkSms(BulkSMSDTO smsdto);
    MultipleTicketResponseDTO getListTickectId(TicketDTO ticketDTO);
}
