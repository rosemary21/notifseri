package com.creditville.notifications.executor;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.requests.HeaderParam;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HttpCallService {

    private static final Logger logger = LoggerFactory.getLogger(HttpCallService.class);

    @Value("${instafin.basic.auth}")
    private String instafinAuth;

    @Value("${paystack.basic.auth}")
    private String psBasicAuth;
    @Value("${tg.paystack.basic.auth}")
    private String tgPsBasicAuth;

    @Value("${instafin.auth.username}")
    private String instafinBasicAuthUsername;

    @Value("${instafin.auth.password}")
    private String instafinBasicAuthPassword;
    @Value("${bulk.sms.apikey}")
    private String smsApikey;

    public String httpPostCall(String url,String payload){
//        logger.info("ENTRY -> Endpoint: {}",url);
        HttpResponse<String> httpResponse = null;
        try {
            httpResponse = Unirest.post(url)
                    .header("Authorization","Basic "+instafinAuth)
                    .header("Content-Type", "application/json")
                    .body(payload)
                    .asString();

        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return httpResponse.getBody();
    }

    public String doBasicPost(String url, String payload) throws CustomCheckedException {
        logger.info("ENTRY doBasicPost -> url: "+url);
        HttpResponse<String> httpResponse;
        try {
            httpResponse = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .basicAuth(instafinBasicAuthUsername, instafinBasicAuthPassword)
                    .body(payload)
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new CustomCheckedException(e.getMessage(), e.getCause());
        }
        if(httpResponse.getStatus() == 200) return  httpResponse.getBody();
        else throw new CustomCheckedException("Unable to perform basic post operation with status code: "+ httpResponse.getStatus() + ". Payload is \n "+ payload);
    }

    public String httpPaystackCall(String url, String payload,boolean isClientTG){
        logger.info("ENTRY -> httpGetCall: {} "+url);
        HttpResponse<String> response = null;
        String basicPsAuth= null;
        if(!isClientTG){
           basicPsAuth = psBasicAuth;
        }else {
            basicPsAuth = tgPsBasicAuth;
        }
        try {
            if(null != payload){
                response = Unirest.post(url)
                        .header("Authorization","Bearer "+ basicPsAuth)
                        .header("Content-Type","application/json")
                        .body(payload)
                        .asString();
            }else {
                response = Unirest.get(url)
                        .header("Authorization","Bearer "+ basicPsAuth)
                        .header("Content-Type","application/json")
                        .asString();
            }

        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }

    public String remitaHttpUrlCall(String url, String payload, HeaderParam headerParam){
        HttpResponse<String> response = null;

        log.info("ENTRY -> remitaHttpUrlCall: {} ",url);
        try {
            if(payload != null && null != headerParam){
                response = Unirest.post(url)
                        .header("Content-Type","application/json")
                        .header("MERCHANT_ID",headerParam.getMerchantId())
                        .header("API_KEY",headerParam.getApiKey())
                        .header("REQUEST_ID",headerParam.getRequestId())
                        .header("REQUEST_TS", String.valueOf(headerParam.getRequest_ts()))
                        .header("API_DETAILS_HASH",headerParam.getApiDetailsHash())
                        .body(payload)
                        .asString();
            }else if(payload != null && null == headerParam){
                response = Unirest.post(url)
                        .header("Content-Type","application/json")
                        .body(payload)
                        .asString();

            } else {

                response = Unirest.get(url)
                        .header("Content-Type","application/json")
                        .asString();
            }

        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }
    public String doBasicSmsPost(String url,String payload){
        HttpResponse<String> response = null;
        log.info("Entry doBasicSmsPost  -> url: {} ",url);
        try {
            response = Unirest.post(url)
                    .header("Accept", "application/json")
                    .header("Authorization", smsApikey)
                    .header("Content-Type", "application/json")
                    .body(payload)
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();

    }

}
