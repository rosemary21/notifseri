package com.creditville.notifications.models.requests;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HeaderParam {
    private String merchantId;
    private String apiKey;
    private String requestId;
    private String request_ts;
    private String apiDetailsHash;
}
