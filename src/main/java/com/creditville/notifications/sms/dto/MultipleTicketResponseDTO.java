package com.creditville.notifications.sms.dto;

import java.util.ArrayList;
import java.util.List;

public class MultipleTicketResponseDTO {

    List<ResponseDTO> dlrs=new ArrayList<>();

    public List<ResponseDTO> getDlrs() {
        return dlrs;
    }

    public void setDlrs(List<ResponseDTO> dlrs) {
        this.dlrs = dlrs;
    }
}
