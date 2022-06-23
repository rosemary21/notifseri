package com.creditville.notifications.utils;


import com.creditville.notifications.models.response.ValidationResp;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;


@Service
public class GeneralUtil {
    public ValidationResp getGeneralErrorMsg(){
        ValidationResp vr = new ValidationResp();
        vr.setCode("76");
        vr.setMessage("An error occurred - Kindly try again later");

        return vr;
    }

    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    public static class DatePattern {
        public static final String firstPattern = "MMM dd, yyyy, HH:mm:ss a"; //Apr 20, 2021, 12:37:25 PM
        public static final String secPattern = "yyyy-MM-dd"; //2021-04-20 12:37:25
        public static final String thirdPattern = "dd/MM/yyyy"; //2021-04-20
        public static final String forthPattern = "DD/MM/YYYY";
    }

    public boolean isClientTG(String clientId){
        return clientId.startsWith("TG");
    }
}
