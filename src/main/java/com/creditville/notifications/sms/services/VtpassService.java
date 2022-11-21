package com.creditville.notifications.sms.services;

import com.creditville.notifications.sms.dto.vtpass.VtpassRequestDto;
import com.creditville.notifications.sms.dto.vtpass.VtpassResponseDto;

public interface VtpassService {

    public VtpassResponseDto sendSms(VtpassRequestDto requestDto);
}
