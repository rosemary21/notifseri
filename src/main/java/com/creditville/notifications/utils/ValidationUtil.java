package com.creditville.notifications.utils;

import com.creditville.notifications.exceptions.CustomCheckedException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Service
public class ValidationUtil {
    public void validatePaystackRequest(HttpServletRequest httpServletRequest) throws CustomCheckedException {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        if(headerNames.hasMoreElements()) {
            String header = httpServletRequest.getHeader("x-paystack-signature");
            if(header == null) throw new CustomCheckedException("Request is not valid. Unable to verify that request is from paystack.");
        }
    }

    public boolean responseContainsValidationError(JSONObject jsonObject){
        String[] validationErrors = new String[] {"LEGACY_VALIDATION_ERROR", "VALIDATION",
                "NON_EXISTING_ACCOUNT", "INVALID_STATUS_CHANGE",
                "ACCOUNT_ALREADY_DISBURSED", "NON_EXISTING_ACCOUNT_STATUS", "VALUE_BEFORE_APPROVAL_DATE",
                "CLIENTS_NOT_FOUND", "PAYMENT_METHOD_UNAVAILABLE", "NON_EXISTING_BRANCH", "STATUS_CHANGE_DATE_INVALID",
                "DISBURSEMENT_NOT_ALLOWED", "GENERIC_VALIDATION_ERROR"};
        boolean contains = false;
        for(String error : validationErrors) {
            if(jsonObject.containsValue(error)) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}
