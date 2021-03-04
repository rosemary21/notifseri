package com.creditville.notifications.executor;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class HttpCallService {

    private static final Logger logger = LoggerFactory.getLogger(HttpCallService.class);

    @Value("${instafin.basic.auth}")
    private String instafinAuth;

    @Value("${paystack.basic.auth}")
    private String psBasicAuth;

    @Value("${instafin.auth.username}")
    private String instafinBasicAuthUsername;

    @Value("${instafin.auth.password}")
    private String instafinBasicAuthPassword;

    public String httpPostCall(String url,String payload){
        logger.info("ENTRY -> Endpoint: {}",url);
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
        logger.info("ENTRY -> Endpoint: {}", url);
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
        else throw new CustomCheckedException("Unable to perform basic post operation with status code: "+ httpResponse.getStatus());
    }

    public String httpPaystackCall(String url, String payload){
        logger.info("ENTRY -> httpGetCall: {} "+url);
        HttpResponse<String> response = null;
        try {
            if(null != payload){
                response = Unirest.post(url)
                        .header("Authorization","Bearer "+ psBasicAuth)
                        .header("Content-Type","application/json")
                        .body(payload)
                        .asString();
            }else {
                response = Unirest.get(url)
                        .header("Authorization","Bearer "+ psBasicAuth)
                        .header("Content-Type","application/json")
                        .asString();
            }

        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }
}
