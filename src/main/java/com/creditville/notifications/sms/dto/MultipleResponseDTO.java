package com.creditville.notifications.sms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class MultipleResponseDTO {

    List<ResponseDTO> responses=new ArrayList<>();


}
