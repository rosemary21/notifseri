package com.creditville.notifications.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class FeeUtil {

    public BigDecimal calculatePaystackFee(BigDecimal amt){
        log.info("amt: "+amt);

        final BigDecimal defaultAmt = new BigDecimal("2500");
        final BigDecimal chargePerc = new BigDecimal("0.015");
        final BigDecimal hundred = new BigDecimal("100");
        final BigDecimal one = new BigDecimal("1.00");
        final BigDecimal maxFee = new BigDecimal("2000");

        BigDecimal feeAmt;

        if(amt.compareTo(defaultAmt) < 0){

            feeAmt = amt.multiply(chargePerc).setScale(2, RoundingMode.CEILING);

        }else {
            feeAmt = amt.multiply(chargePerc).add(hundred).setScale(2,RoundingMode.CEILING);
        }
        log.info("feeAmt: "+feeAmt);

        if(feeAmt.compareTo(maxFee) > 0){
            return maxFee;
        }
        return feeAmt;
    }
}
