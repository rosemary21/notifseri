package com.creditville.notifications.models.DTOs;

import com.creditville.notifications.models.requests.MandateParam;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public class DebitInstructionDTO  extends MandateParam implements Serializable {
    private String merchantId;
    private String serviceTypeId;
    private String hash;
    private String mandateId;
    private String requestId;
    private String totalAmount;
    private String fundingAccount;
    private String fundingBankCode;
}
