package com.creditville.notifications.sms.dto.vtpass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VtpassResponseDto {

    private String responseCode;
    private String response;
    private String batchId;
    private String clientBatchId;
    private String sentDate;
    private List<messagesList> messages;

    @Getter
    @Setter
    public static class messagesList{
        private String statusCode;
        private String recipient;
        private String messageId;
        private String status;
        private String description;
        private String network;
        private String country;
        private String deliveryCode;
        private String deliveryDate;
        private String bulkId;
    }

}

