package com.creditville.notifications.sms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class MultipleTicketResponseDTO {

    List<ResponseDTO> dlrs=new ArrayList<>();

}
