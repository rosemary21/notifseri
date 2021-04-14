package com.creditville.notifications.utils;

import com.creditville.notifications.exceptions.CustomCheckedException;
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
}
