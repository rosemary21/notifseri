package com.creditville.notifications.sms.dto;

import java.util.ArrayList;
import java.util.List;

public class MultipleResponseDTO {

    List<ResponseDTO> responses=new ArrayList<>();

    public List<ResponseDTO> getResponses() {
        return responses;
    }

    public void setResponses(List<ResponseDTO> responses) {
        this.responses = responses;
    }
}
